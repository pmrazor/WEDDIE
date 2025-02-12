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
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity; // 추가
import org.springframework.http.HttpMethod; // 추가
import org.springframework.http.MediaType; // 추가

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
    private final PaymentConverter paymentConverter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Authorization 헤더 생성 메서드 추가
    private String getAuthorizationHeader() {
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        return "Basic " + encodedKey;
    }

    // 결제 요청
    @Override
    public PaymentResponse.CreateResultDTO createPayment(PaymentRequest.CreateDTO paymentRequest) {
        // 결제 객체 생성 (Demand 없이 결제만)
        Payment payment = Payment.builder()
                .amount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.READY)
                .build();

        paymentRepository.save(payment);  // 결제 정보 저장

        // DTO로 변환하여 반환
        return paymentConverter.toCreateResultDTO(payment);
    }

    @Override
    public PaymentResponse.ConfirmResultDTO confirmPayment(PaymentRequest.ConfirmDTO confirmRequest) {
        try {
            log.info("✅ [confirmPayment] 결제 승인 요청 시작: {}", confirmRequest);  // 요청 DTO 로그 출력

            // 1. 결제 ID 확인
            Long paymentId = confirmRequest.getPaymentId();
            if (paymentId == null) {
                log.error("❌ [confirmPayment] paymentId가 null입니다.");
                throw new IllegalArgumentException("결제 ID (paymentId)가 null입니다.");
            }
            log.info("✅ [confirmPayment] 조회할 paymentId: {}", paymentId);

            // 2. 결제 정보 조회
            Payment payment = getPayment(paymentId);
            log.info("✅ [confirmPayment] 결제 정보 조회 성공: {}", payment);

            // 3. 결제 승인 API 요청 생성
            String requestBody = String.format("{\"orderId\":\"%s\", \"amount\":%d}",
                    payment.getOrderId(), payment.getAmount());
            log.info("✅ [confirmPayment] Toss API 요청 본문: {}", requestBody);

            // 4. 결제 승인 요청 (API 호출 예시)
            String authorizationHeader = getAuthorizationHeader();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(paymentApiUrl + "/v1/payments/confirm"))
                    .header("Authorization", authorizationHeader)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("✅ [confirmPayment] Toss API 응답 코드: {}", response.statusCode());
            log.info("✅ [confirmPayment] Toss API 응답 본문: {}", response.body());

            if (response.statusCode() == 200) {
                // 결제 승인 성공 시 결제 상태 업데이트
                payment.setPaymentStatus(PaymentStatus.DONE);
                payment.setApprovedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                log.info("✅ [confirmPayment] 결제 승인 완료: paymentId={}, 상태={}", payment.getPaymentId(), payment.getPaymentStatus());

                // `Payment` 엔티티를 `PaymentResponse.ConfirmResultDTO`로 변환하여 반환
                return PaymentResponse.ConfirmResultDTO.builder()
                        .paymentId(payment.getPaymentId())
                        .paymentKey(payment.getPaymentKey())
                        .orderId(payment.getOrderId())
                        .amount(payment.getAmount())
                        .orderName(payment.getOrderName())
                        .paymentMethod(payment.getPaymentMethod().name())  // Enum을 String으로 변환
                        .paymentStatus(payment.getPaymentStatus().name())  // Enum을 String으로 변환
                        .requestedAt(payment.getRequestedAt().toString())  // LocalDateTime을 String으로 변환
                        .approvedAt(payment.getApprovedAt() != null ? payment.getApprovedAt().toString() : null)  // 결제 승인 시간이 null일 수 있음
                        .build();
            } else {
                log.error("❌ [confirmPayment] 결제 승인 실패 - 상태 코드: {}, 응답: {}", response.statusCode(), response.body());
                throw new RuntimeException("결제 승인 실패, 상태 코드: " + response.statusCode());
            }

        } catch (Exception e) {
            log.error("❌ [confirmPayment] 결제 승인 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("❌ 결제 승인 오류 발생: " + e.getMessage());
        }
    }



    // 결제 조회 메서드 추가
    private Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
    }
}
