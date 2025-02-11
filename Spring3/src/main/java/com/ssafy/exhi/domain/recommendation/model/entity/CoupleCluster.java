package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoupleCluster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Couple <-> CoupleCluster : 1:1 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id") // 실제 FK 컬럼명
    private Couple couple;

    // 어떤 클러스터에 속해 있는지 식별할 수 있는 값
    private Integer clusterId;

    // 스케일 변환된 좌표값들
    private Double maleAgeCoordinate;
    private Double femaleAgeCoordinate;
    private Double ageDiffCoordinate;
    private Double budgetCoordinate;
    private Double marriageMonthSinCoordinate;
    private Double marriageMonthCosCoordinate;
    private Double marriageDaySinCoordinate;
    private Double marriageDayCosCoordinate;
    private Double userClusterDistanceCoordinate;
} 