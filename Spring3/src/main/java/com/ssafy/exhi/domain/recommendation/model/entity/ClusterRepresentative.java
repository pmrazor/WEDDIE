package com.ssafy.exhi.domain.recommendation.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.user.model.entity.User;
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
public class ClusterRepresentative extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer clusterId;  // 고유 클러스터 식별 값

    // 대표 유저
    @OneToOne
    @JoinColumn(name = "representative_user_id")
    private User representativeUser;

    public void setRepresentativeUser(User representativeUser) {
        this.representativeUser = representativeUser;
    }

}