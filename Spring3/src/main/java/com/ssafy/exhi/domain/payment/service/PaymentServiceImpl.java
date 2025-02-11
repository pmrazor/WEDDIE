package com.ssafy.exhi.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import com.ssafy.exhi.domain.payment.model.entity.Payment;
import com.ssafy.exhi.domain.payment.model.entity.PaymentStatus;
import com.ssafy.exhi.domain.payment.model.entity.PaymentMethod;
import com.ssafy.exhi.domain.payment.repository.PaymentRepository;
import com.ssafy.exhi.domain.order.model.entity.Order;
import com.ssafy.exhi.domain.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${payment.api.url}")
    private String paymentApiUrl;

    @Value("${payment.secret.key}")
    private String secretKey;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository; // ✅ `Order` 관련 Repository 추가
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // ✅ 결제 요청 (결제 요청 정보를 DB에 저장)
    @Override
    public PaymentResponse.CreateResultDTO createPayment(PaymentRequest.CreateDTO paymentRequest) {
        // ✅ Order 엔티티 조회
        Order order = orderRepository.findById(Long.valueOf(paymentRequest.getOrderId()))
                .orElseThrow(() -> new RuntimeException("해당 orderId에 대한 주문을 찾을 수 없습니다."));

        // ✅ 결제 요청 정보를 DB에 저장
        Payment payment = Payment.builder()
                .order(order) // ✅ `Order` 객체 저장
                .totalAmount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.READY) // 결제 대기 상태
                .requestedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        return PaymentResponse.CreateResultDTO.builder()
                .orderId(order.getId().toString())
                .orderName(order.getOrderName()) // ✅ `Order`의 주문명 반환
                .totalAmount(paymentRequest.getAmount())
                .status("READY")
                .build();
    }

    // ✅ 결제 승인 (결제 승인 API 요청 & Order 연동)
    @Override
    public PaymentResponse.CreateResultDTO confirmPayment(PaymentRequest.ConfirmDTO confirmRequest) {
        try {
            String paymentKey = confirmRequest.getPaymentKey();
            int amount = confirmRequest.getAmount();
            String orderId = confirmRequest.getOrderId(); // ✅ orderId는 Order 객체와 연결됨

            // ✅ Order 조회
            Order order = orderRepository.findById(Long.valueOf(orderId))
                    .orElseThrow(() -> new RuntimeException("해당 orderId에 대한 주문을 찾을 수 없습니다."));

            // ✅ JSON 요청 객체 생성
            JsonNode requestObj = objectMapper.createObjectNode()
                    .put("orderId", orderId)
                    .put("amount", amount)
                    .put("paymentKey", paymentKey);

            String requestBody = objectMapper.writeValueAsString(requestObj);

            // ✅ 결제 승인 API 요청
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(paymentApiUrl + "/v1/payments/confirm")) // Toss API 제거 후 일반 API 적용
                    .header("Authorization", getAuthorizationHeader())
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> paymentData = objectMapper.readValue(response.body(), Map.class);

                // ✅ 기존 결제 정보 조회
                Payment payment = paymentRepository.findByOrder(order)
                        .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

                // ✅ 결제 정보 업데이트
                payment.setPaymentKey(paymentKey);
                payment.setPaymentStatus(PaymentStatus.valueOf("DONE"));
                payment.setApprovedAt(LocalDateTime.parse((String) paymentData.get("approvedAt")));

                // ✅ 변경된 결제 정보 저장
                paymentRepository.save(payment);

                return PaymentResponse.CreateResultDTO.builder()
                        .paymentKey(paymentKey)
                        .orderId(order.getId().toString())
                        .orderName(order.getOrderName())
                        .totalAmount(amount)
                        .status("DONE")
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
