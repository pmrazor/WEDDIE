package com.ssafy.exhi.domain.payment.model.entity;

import com.ssafy.exhi.domain.demand.model.entity.Demand; // Order 엔티티 import
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;


    // toss에서 제공하는 결제 키
    @Column(nullable = false, unique = true)
    String paymentKey;

    // Order 엔티티와의 관계 설정
    @OneToOne
    @JoinColumn(name = "demand_id", nullable = false)
    Demand demand;

    // 총 결제 금액
    long totalAmount;

    // 결제 방식 (PaymentMethod.java)
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod paymentMethod;

    // 결제 상태 (PaymentStatus.java)
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus paymentStatus;

    // 결제 요청 시간
    @Column(nullable = false)
    LocalDateTime requestedAt;

    // 결제 승인 시간
    LocalDateTime approvedAt;

    // 기타 메서드 생략
}

