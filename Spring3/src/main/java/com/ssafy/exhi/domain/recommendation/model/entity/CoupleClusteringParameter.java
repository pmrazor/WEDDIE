package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleClusteringParameter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // male_age scaler
    private Double maleAgeMean;
    private Double maleAgeVar;
    private Double maleAgeScale;

    // female_age scaler
    private Double femaleAgeMean;
    private Double femaleAgeVar;
    private Double femaleAgeScale;

    // age_diff scaler
    private Double ageDiffScale;
    private Double ageDiffMin;
    private Double ageDiffDataMin;
    private Double ageDiffDataMax;

    // budget scaler
    private Double budgetScale;
    private Double budgetMin;
    private Double budgetDataMin;
    private Double budgetDataMax;

    // marriage month/day sin, cos scaler
    private Double marriageMonthSinScale;
    private Double marriageMonthSinMin;
    private Double marriageMonthSinDataMin;
    private Double marriageMonthSinDataMax;

    private Double marriageMonthCosScale;
    private Double marriageMonthCosMin;
    private Double marriageMonthCosDataMin;
    private Double marriageMonthCosDataMax;

    private Double marriageDaySinScale;
    private Double marriageDaySinMin;
    private Double marriageDaySinDataMin;
    private Double marriageDaySinDataMax;

    private Double marriageDayCosScale;
    private Double marriageDayCosMin;
    private Double marriageDayCosDataMin;
    private Double marriageDayCosDataMax;

    // userClusterDistance scaler
    private Double userClusterDistScale;
    private Double userClusterDistMin;
    private Double userClusterDistDataMin;
    private Double userClusterDistDataMax;

    // 최적화된 가중치
    private Double maleAgeWeight;
    private Double femaleAgeWeight;
    private Double ageDiffWeight;
    private Double budgetWeight;
    private Double marriageMonthSinWeight;
    private Double marriageMonthCosWeight;
    private Double marriageDaySinWeight;
    private Double marriageDayCosWeight;
    private Double userClusterDistanceWeight;

} 