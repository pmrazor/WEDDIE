package com.ssafy.exhi.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import com.ssafy.exhi.domain.payment.model.entity.Payment;
import com.ssafy.exhi.domain.payment.model.entity.PaymentStatus;
import com.ssafy.exhi.domain.payment.repository.PaymentRepository;
import com.ssafy.exhi.domain.demand.model.entity.Demand;
import com.ssafy.exhi.domain.demand.repository.DemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.time.LocalDateTime;

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
    private final DemandRepository demandRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ 결제 요청 (결제 요청 정보를 DB에 저장)
    @Override
    public PaymentResponse.CreateResultDTO createPayment(PaymentRequest.CreateDTO paymentRequest) {
        // Demand 엔티티 조회
        Demand demand = demandRepository.findById(paymentRequest.getDemandId())
                .orElseThrow(() -> new RuntimeException("해당 demandId에 대한 주문을 찾을 수 없습니다."));

        // 결제 요청 정보를 DB에 저장
        Payment payment = Payment.builder()
                .demand(demand) // Demand 객체 저장
                .totalAmount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.READY) // 결제 대기 상태
                .requestedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        return PaymentResponse.CreateResultDTO.builder()
                .demandId(demand.getId())
                .demandName(demand.getDemandName())
                .totalAmount(paymentRequest.getAmount())
                .status(PaymentStatus.READY.name()) // Enum의 문자열 값 반환
                .build();
    }

    // ✅ 결제 승인 (결제 승인 API 요청 & Demand 연동)
    @Override
    public PaymentResponse.CreateResultDTO confirmPayment(PaymentRequest.ConfirmDTO confirmRequest) {
        try {
            String paymentKey = confirmRequest.getPaymentKey();
            int amount = confirmRequest.getAmount();
            Long demandId = confirmRequest.getDemandId();

            // Demand 조회
            Demand demand = demandRepository.findById(demandId)
                    .orElseThrow(() -> new RuntimeException("해당 demandId에 대한 주문을 찾을 수 없습니다."));

            // JSON 요청 객체 생성
            JsonNode requestObj = objectMapper.createObjectNode()
                    .put("demandId", demandId)
                    .put("amount", amount)
                    .put("paymentKey", paymentKey);

            String requestBody = objectMapper.writeValueAsString(requestObj);

            // 결제 승인 API 요청
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(paymentApiUrl + "/v1/payments/confirm"))
                    .header("Authorization", getAuthorizationHeader())
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> paymentData = objectMapper.readValue(response.body(), Map.class);

                // 기존 결제 정보 조회
                Payment payment = paymentRepository.findByDemand(demand)
                        .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

                // ✅ 결제 정보 업데이트
                payment.setPaymentKey(paymentKey);
                payment.setPaymentStatus(PaymentStatus.DONE); // ✅ ENUM 직접 사용
                payment.setApprovedAt(LocalDateTime.parse((String) paymentData.get("approvedAt")));

                // ✅ 변경된 결제 정보 저장
                paymentRepository.save(payment);

                return PaymentResponse.CreateResultDTO.builder()
                        .paymentKey(paymentKey)
                        .demandId(demand.getId())
                        .demandName(demand.getDemandName())
                        .totalAmount(amount)
                        .status(PaymentStatus.DONE.name()) // ✅ Enum의 문자열 값 반환
                        .paymentMethod((String) paymentData.get("method"))
                        .build();
            } else {
                throw new RuntimeException("결제 승인 실패: " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("결제 승인 오류 발생: " + e.getMessage());
        }
    }

    private String getAuthorizationHeader() {
        return "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
    }
}
