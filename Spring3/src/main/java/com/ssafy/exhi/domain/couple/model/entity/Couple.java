package com.ssafy.exhi.domain.couple.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleCluster;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "couples")
public class Couple extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_id")
    private Integer id;

    @Column(name = "couple_name")
    private String coupleName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "female_id", unique = true)
    private User female;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "male_id", unique = true)
    private User male;

    @OneToOne(mappedBy = "couple")
    private VirginRoad virginRoad;

    // for recommend

    @Column(name = "budget")
    private Integer budget;

    @Column(name = "marriage_date")
    private LocalDate marriageDate;

    @OneToOne(mappedBy = "couple", cascade = CascadeType.ALL, orphanRemoval = true)
    private CoupleCluster coupleCluster;

    public void setCoupleCluster(CoupleCluster coupleCluster) {
        this.coupleCluster = coupleCluster;
        coupleCluster.setCouple(this);
    }

}
