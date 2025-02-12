package com.ssafy.exhi.domain.payment.model.entity;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false, unique = true)
    private String paymentKey;  // Toss 결제 키

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private long amount;  // 결제 금액

    @Column(nullable = false)
    private String orderName;  // 주문 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;  // 결제 방법 (예: 카드, 계좌이체 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;  // 결제 상태 (예: READY, DONE 등)

    @Column(nullable = false)
    private LocalDateTime requestedAt;  // 결제 요청 시각

    private LocalDateTime approvedAt;  // 결제 승인 시각 (optional)

    // Entity를 응답 DTO로 변환하는 메서드
    public PaymentResponse.ConfirmResultDTO toConfirmResultDTO() {
        return PaymentResponse.ConfirmResultDTO.builder()
                .paymentId(this.paymentId)
                .paymentKey(this.paymentKey)
                .orderId(this.orderId)
                .amount(this.amount)
                .orderName(this.orderName)
                .paymentMethod(this.paymentMethod.name()) // PaymentMethod를 문자열로 변환
                .paymentStatus(this.paymentStatus.name()) // PaymentStatus를 문자열로 변환
                .requestedAt(this.requestedAt.toString()) // LocalDateTime을 String으로 변환
                .approvedAt(this.approvedAt != null ? this.approvedAt.toString() : null) // 결제 승인 시간이 null일 수 있음
                .build();
    }
}
