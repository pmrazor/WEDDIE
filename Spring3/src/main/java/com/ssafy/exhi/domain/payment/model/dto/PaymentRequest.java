package com.ssafy.exhi.domain.payment.model.dto;

import lombok.*;
import com.ssafy.exhi.domain.payment.model.entity.PaymentMethod;
import com.ssafy.exhi.domain.payment.model.entity.Payment;
import java.util.UUID;



@Getter
@Setter
public class PaymentRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        private Long demandId;     // 주문 ID (고유 값, Toss 결제 요청 시 사용)
        private String demandName;   // 주문 이름
        private int amount;         // 결제 금액
        private String successUrl;  // 결제 성공 후 이동할 URL
        private String failUrl;     // 결제 실패 후 이동할 URL
        private String customerEmail;  // 고객 이메일
        private String customerName;   // 고객 이름
        private String customerMobilePhone;  // 고객 휴대폰 번호
    }

//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class ConfirmDTO {
//        private String paymentKey;  // Toss 결제 키
//        private String orderId;     // 주문 ID
//        private Number amount;         // 결제 금액
//    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmDTO {
        private Long paymentId;
        private PaymentMethod paymentMethod;
        private Long amount;
        private String orderName;
        private String yourSuccessUrl;
        private String yourFailUrl;

        public Payment toEntity() {
            return Payment.builder()
                    .paymentMethod(this.paymentMethod)
                    .amount(amount)
                    .orderName(orderName)
                    .orderId(UUID.randomUUID().toString())
                    .build();
        }
    }
}

