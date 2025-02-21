package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserClusterCenters extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 1:N 양방향 관계 : 한 묶음(이 엔티티)에 다수의 UserClusterCenter 속함 */
    @OneToMany(mappedBy = "userClusterCenters", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserClusterCenter> userClusterCenterList = new ArrayList<>();

    // 편의 메서드
    public void addUserClusterCenter(UserClusterCenter center) {
        userClusterCenterList.add(center);
        center.setUserClusterCenters(this);
    }
}
