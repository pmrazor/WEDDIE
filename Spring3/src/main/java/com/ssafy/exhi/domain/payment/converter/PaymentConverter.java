package com.ssafy.exhi.domain.payment.converter;

import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import com.ssafy.exhi.domain.payment.model.entity.Payment;
import com.ssafy.exhi.domain.payment.model.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PaymentConverter {

    // ✅ CreateDTO → Payment 엔티티 변환
    public Payment toEntity(PaymentRequest.CreateDTO paymentRequest) {
        return Payment.builder()
                .amount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.READY) // 기본값: READY
                .requestedAt(LocalDateTime.now())
                .build();
    }

    // ✅ Payment 엔티티 → CreateResultDTO 변환
    public PaymentResponse.CreateResultDTO toCreateResultDTO(Payment payment) {
        return PaymentResponse.CreateResultDTO.builder()
                .amount(payment.getAmount())
                .status(payment.getPaymentStatus().name())
                .build();
    }

    // ✅ 결제 승인 후 응답 DTO 변환 (결제 승인 후 응답)
    public PaymentResponse.ConfirmResultDTO toConfirmResultDTO(Payment payment, Map<String, Object> paymentData) {
        return PaymentResponse.ConfirmResultDTO.builder()
                .paymentId(payment.getPaymentId())  // 결제 ID
                .paymentKey(payment.getPaymentKey())  // 결제 고유 키
                .orderId(payment.getOrderId())  // 주문 번호
                .amount(payment.getAmount())  // 결제 금액
                .orderName(payment.getOrderName())  // 주문 이름
                .paymentMethod((String) paymentData.get("method"))  // 결제 방식 (예: 카드, 가상계좌 등)
                .paymentStatus(payment.getPaymentStatus().name())  // 결제 상태
                .requestedAt(payment.getRequestedAt().toString())  // 결제 요청 시각
                .approvedAt(payment.getApprovedAt() != null ? payment.getApprovedAt().toString() : null)  // 결제 승인 시각
                .build();
    }
}
