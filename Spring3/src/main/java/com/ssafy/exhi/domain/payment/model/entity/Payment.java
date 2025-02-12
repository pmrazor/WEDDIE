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
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ PK 자동 생성
    private Long paymentId;  // byte[] → Long 타입으로 변경!

    @Column(nullable = false, unique = true)
    private String paymentKey;

    // ✅ Demand와 OneToOne 관계
    @OneToOne
    @JoinColumn(name = "demand_id", nullable = false, unique = true) // ✅ FK 설정
    private Demand demand;

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
}

