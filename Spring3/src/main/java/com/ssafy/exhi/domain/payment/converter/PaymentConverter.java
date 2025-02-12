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
                .totalAmount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.READY) // 기본값: READY
                .requestedAt(LocalDateTime.now())
                .build();
    }

    // ✅ Payment 엔티티 → CreateResultDTO 변환
    public PaymentResponse.CreateResultDTO toCreateResultDTO(Payment payment) {
        return PaymentResponse.CreateResultDTO.builder()
                .paymentKey(payment.getPaymentKey())  // paymentKey가 결제에 고유값으로 들어감
                .totalAmount(payment.getTotalAmount())
                .status(payment.getPaymentStatus().name())
                .build();
    }

    // ✅ 결제 승인 후 응답 DTO 변환
    public PaymentResponse.CreateResultDTO toConfirmResultDTO(Payment payment, Map<String, Object> paymentData) {
        return PaymentResponse.CreateResultDTO.builder()
                .paymentKey(payment.getPaymentKey())
                .totalAmount(payment.getTotalAmount())
                .status(payment.getPaymentStatus().name())
                .paymentMethod((String) paymentData.get("method"))  // 예시로 결제 방식 추가
                .build();
    }
}
