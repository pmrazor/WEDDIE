package com.ssafy.exhi.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import com.ssafy.exhi.domain.payment.model.entity.Payment;
import com.ssafy.exhi.domain.payment.model.entity.PaymentStatus;
import com.ssafy.exhi.domain.payment.converter.PaymentConverter;
import com.ssafy.exhi.domain.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${payment.api.url}")
    private String paymentApiUrl;

    @Value("${payment.secret.key}")
    private String secretKey;

    private final PaymentRepository paymentRepository;
    private final PaymentConverter paymentConverter;  // PaymentConverter 주입
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // ✅ 결제 요청
    @Override
    public PaymentResponse.CreateResultDTO createPayment(PaymentRequest.CreateDTO paymentRequest) {
        // 결제 객체 생성 (Demand 없이 결제만)
        Payment payment = Payment.builder()
                .totalAmount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.READY)
                .build();

        paymentRepository.save(payment);  // 결제 정보 저장

        // DTO로 변환하여 반환
        return paymentConverter.toCreateResultDTO(payment);
    }

    // ✅ 결제 승인
    @Override
    public PaymentResponse.CreateResultDTO confirmPayment(PaymentRequest.ConfirmDTO confirmRequest) {
        try {
            String paymentKey = confirmRequest.getPaymentKey();
            String orderId = confirmRequest.getOrderId();  // orderId 사용
            Number amount = confirmRequest.getAmount();

            // 결제 찾기 (paymentKey로 찾기)
            Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                    .orElseThrow(() -> new RuntimeException("❌ 해당 결제 정보를 찾을 수 없습니다."));

            // 결제 승인 처리
            String requestBody = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("paymentKey", paymentKey)
                            .put("orderId", orderId)
                            .put("amount", amount.intValue())  // amount는 int로 변환
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(paymentApiUrl + "/v1/payments/confirm"))
                    .header("Authorization", getAuthorizationHeader())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // 결제 승인 후 상태 업데이트
                Map<String, Object> paymentData = objectMapper.readValue(response.body(), Map.class);

                payment.setPaymentStatus(PaymentStatus.DONE);  // PaymentStatus enum 사용
                paymentRepository.save(payment);

                return paymentConverter.toConfirmResultDTO(payment, paymentData);  // Payment 엔티티에서 DTO로 변환
            } else {
                throw new RuntimeException("❌ 결제 승인 실패: " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ 결제 승인 오류 발생: " + e.getMessage());
        }
    }

    private String getAuthorizationHeader() {
        return "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
    }
}
