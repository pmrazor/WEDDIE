package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClusterCenters extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 1:N 양방향 관계 : 한 묶음(이 엔티티)에 다수의 ClusterCenter들이 속함 */
    @OneToMany(mappedBy = "clusterCenters", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClusterCenter> clusterCenterList = new ArrayList<>();

    // 편의 메서드
    public void addClusterCenter(ClusterCenter center) {
        clusterCenterList.add(center);
        center.setClusterCenters(this);
    }
}
