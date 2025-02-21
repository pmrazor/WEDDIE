package com.ssafy.exhi.domain.virginroad.model.entity;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VirginRoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "virgin_road_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", unique = true)
    private Couple couple;

    @OneToMany(mappedBy = "virginRoad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plan> plans = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "virgin_road_status")
    private PlanStatus virginRoadStatus;

    /* 비즈니스 로직 */

    public void finishVirginRoad() {
        this.virginRoadStatus = PlanStatus.FINISHED;
    }

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

    public void setDefaultPlans(Map<ServiceType, List<DefaultTodo>> defaultTodos) {
        for (ServiceType serviceType : ServiceType.values()) {
            Plan plan = Optional.ofNullable(this.findPlanByServiceType(serviceType))
                .orElse(Plan.createDefault(serviceType, this));

            plan.setDefaultTodos(serviceType, defaultTodos);
            plan.calculateStartAndEndDates();
            addPlan(plan);
        }
    }

    public Plan findPlanByServiceType(ServiceType serviceType) {
        return plans.stream()
            .filter(plan -> plan.getServiceType() == serviceType)
            .findFirst()
            .orElse(null);
    }
    /* 비즈니스 로직 */

}
