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

    public static class CreateResultDTO {
        private Long amount;    // 결제 금액
        private String OrderId;      // 주문 번호
        private String orderName;    // 주문 이름
        private String status;       // 결제 상태 (READY, DONE, CANCELED 등)
        private String paymentMethod; // 결제 방법 (카드, 가상계좌 등)
    }

    // 결제 승인 후 응답 DTO 추가
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmResultDTO {
        private Long paymentId;   // 결제 ID
        private String paymentKey;  // Toss 결제 키
        private String orderId;    // 주문 번호
        private long amount;       // 결제 금액
        private String orderName;  // 주문 이름
        private String paymentMethod; // 결제 방법
        private String paymentStatus; // 결제 상태 (예: READY, DONE 등)
        private String requestedAt; // 결제 요청 시각
        private String approvedAt;  // 결제 승인 시각
    }
}
