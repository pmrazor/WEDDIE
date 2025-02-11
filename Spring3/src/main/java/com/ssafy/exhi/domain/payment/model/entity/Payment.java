package com.ssafy.exhi.domain.payment.model.entity;

import com.ssafy.exhi.domain.order.model.entity.Order; // Order 엔티티 import
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    byte[] paymentId;

    // toss에서 제공하는 결제 키
    @Column(nullable = false, unique = true)
    String PaymentKey;

    // toss 내부 주문번호
    @Column(nullable = false)
    String OrderId;

    // Order 엔티티와의 관계 설정
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    // 총 결제 금액
    long totalAmount;

    // 결제 방식 (PaymentMethod.java)
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod PaymentMethod;

    // 결제 상태 (PaymentStatus.java)
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus PaymentStatus;

    // 결제 요청 시간
    @Column(nullable = false)
    LocalDateTime requestedAt;

    // 결제 승인 시간
    LocalDateTime approvedAt;

    // 기타 메서드 생략
}

