package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.domain.TimeWrapper;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;

public interface PlanService {
    PlanResponse.DetailResultDTO getPlan(Integer userId, Integer planId);

    PlanResponse.AllResultDTO getAllPlans(Integer userId);

    void allPlanUpdate(TimeWrapper timeWrapper);
}