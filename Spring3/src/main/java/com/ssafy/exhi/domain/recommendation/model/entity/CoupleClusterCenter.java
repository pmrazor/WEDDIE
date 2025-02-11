package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoupleClusterCenter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer clusterId;

    private Double maleAgeCoordinate;
    private Double femaleAgeCoordinate;
    private Double ageDiffCoordinate;
    private Double budgetCoordinate;
    private Double marriageMonthSinCoordinate;
    private Double marriageMonthCosCoordinate;
    private Double marriageDaySinCoordinate;
    private Double marriageDayCosCoordinate;
    private Double userClusterDistanceCoordinate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_cluster_centers_id")
    private CoupleClusterCenters coupleClusterCenters;
}
