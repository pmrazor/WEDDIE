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
public class ClusterCenter extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private Integer clusterId;
    
    private Double ageCoordinate;
    private Double budgetCoordinate;
    
    // MBTI 4차원 좌표
    private Double mbtiIeCoordinate;
    private Double mbtiSnCoordinate;
    private Double mbtiTfCoordinate;
    private Double mbtiJpCoordinate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_centers_id")
    private ClusterCenters clusterCenters;
} 