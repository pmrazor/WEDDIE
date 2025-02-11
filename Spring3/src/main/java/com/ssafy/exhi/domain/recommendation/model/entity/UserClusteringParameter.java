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
public class UserClusteringParameter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // age scaler
    private Double ageMean;
    private Double ageVar;
    private Double ageScale;

    // budget scaler
    private Double budgetScale;
    private Double budgetMin;
    private Double budgetDataMin;
    private Double budgetDataMax;

    // MBTI는 스케일링 파라미터 불필요 (one-hot encoding)

    // 최적화된 가중치
    private Double ageWeight;
    private Double budgetWeight;
    private Double mbtiIeWeight;
    private Double mbtiSnWeight;
    private Double mbtiTfWeight;
    private Double mbtiJpWeight;
} 