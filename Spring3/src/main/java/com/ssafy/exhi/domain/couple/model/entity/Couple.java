package com.ssafy.exhi.domain.couple.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.couple.converter.CoupleConverter;
import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleCluster;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // 결혼 예정일
    @Column(name = "marriage_date")
    private LocalDate marriageDate;

//    // 사귀기 시작한 날짜
    @Column(name = "love_anniversary")
    private LocalDate loveAnniversary;

    @OneToOne(mappedBy = "couple", cascade = CascadeType.ALL, orphanRemoval = true)
    private CoupleCluster coupleCluster;

    public void setCoupleCluster(CoupleCluster coupleCluster) {
        this.coupleCluster = coupleCluster;
        coupleCluster.setCouple(this);
    }

    public CoupleResponse.DetailResultDTO updateCouple(CoupleRequest.UpdateDTO updateDTO) {
        this.marriageDate = updateDTO.getMarriageDate();
        this.budget = updateDTO.getBudget();
        this.loveAnniversary = updateDTO.getLoveAnniversary();

        return CoupleConverter.toDetailDTO(this);
    }
}
