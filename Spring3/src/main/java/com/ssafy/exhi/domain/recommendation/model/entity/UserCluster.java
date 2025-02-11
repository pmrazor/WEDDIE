package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCluster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // User <-> UserCluster : 1:1 관계
    // User 엔티티에 있는 'userCluster' 필드와 매핑됨
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 실제 FK 컬럼명
    private User user;

    // 어떤 클러스터에 속해 있는지 식별할 수 있는 값 (예: cluster_id)
    private Integer clusterId;

    // 스케일 변환된 좌표값들
    private Double ageCoordinate;
    private Double budgetCoordinate;
    private Double mbtiIeCoordinate;
    private Double mbtiSnCoordinate;
    private Double mbtiTfCoordinate;
    private Double mbtiJpCoordinate;
}
