package com.ssafy.exhi.domain.plan.repository;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findPlanById(Integer id);
}
