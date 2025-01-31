package com.ssafy.exhi.domain.plan.repository;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

}
