package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClusteringParameter extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    // Scaler parameters
    private Double ageMean;
    private Double ageScale;
    private Double budgetMin;
    private Double budgetMax;
    
    // Feature weights
    private Double ageWeight;
    private Double budgetWeight;
    private Double mbtiIeWeight;
    private Double mbtiSnWeight;
    private Double mbtiTfWeight;
    private Double mbtiJpWeight;
} 