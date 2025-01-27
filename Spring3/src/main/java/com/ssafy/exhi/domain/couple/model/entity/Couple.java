package com.ssafy.exhi.domain.couple.model.entity;


import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
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
@Table(name = "couples")
public class Couple {
    @Id
    @GeneratedValue
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virgin_road_id")
    private VirginRoad virginRoad;
}
