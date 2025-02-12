package com.ssafy.exhi.domain.payment.model.entity;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ PK 자동 생성
    private Long paymentId;  // byte[] → Long 타입으로 변경!

    @Column(nullable = false, unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private String orderId;  // 주문 ID 추가
}

