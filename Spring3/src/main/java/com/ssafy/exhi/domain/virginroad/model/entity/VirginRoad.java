package com.ssafy.exhi.domain.virginroad.model.entity;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VirginRoad {

    @Id
    @GeneratedValue
    @Column(name = "virgin_road_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @OneToMany(mappedBy = "virginRoad")
    private List<Plan> plans = new ArrayList<>();

    public VirginRoad addPlans(List<Plan> plans) {
        for (Plan plan : plans) {
            addPlan(plan);
        }
        return this;
    }

    public void addPlan(Plan plan) {
        plan.updateVirginRoad(this);

        if (!plans.contains(plan)) {
            plans.add(plan);
        }
    }
}
