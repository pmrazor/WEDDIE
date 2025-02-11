package com.ssafy.exhi.domain.payment.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class PaymentResponse {

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor

    public static class PaymentDTO {
        private String paymentKey;   // 결제 키
        private String orderId;      // 주문 번호
        private String orderName;    // 주문 이름
        private long totalAmount;    // 결제 금액
        private String status;       // 결제 상태 (READY, DONE, CANCELED 등)
        private String paymentMethod; // 결제 방법 (카드, 가상계좌 등)
    }
}
