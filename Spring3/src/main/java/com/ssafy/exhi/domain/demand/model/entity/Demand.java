package com.ssafy.exhi.domain.demand.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Demand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String demandId; // 주문번호 (고유 값)

    @Column(nullable = false)
    private String demandName; // 주문명 (예: "토스 티셔츠 외 2건")

    @Column(nullable = false)
    private long totalAmount; // 총 주문 금액

    @Column(nullable = false)
    private LocalDateTime createdAt; // 주문 생성 시간
}
