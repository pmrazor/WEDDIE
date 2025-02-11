package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleClusterCenters extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 1:N 양방향 관계 : 한 묶음(이 엔티티)에 다수의 CoupleClusterCenter 속함 */
    @OneToMany(mappedBy = "coupleClusterCenters", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoupleClusterCenter> coupleClusterCenterList = new ArrayList<>();

    // 편의 메서드
    public void addCoupleClusterCenter(CoupleClusterCenter center) {
        coupleClusterCenterList.add(center);
        center.setCoupleClusterCenters(this);
    }
}
