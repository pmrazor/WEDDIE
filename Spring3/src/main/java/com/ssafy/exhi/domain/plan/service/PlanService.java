package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse.SimpleResultDTO;

public interface PlanService {
    SimpleResultDTO createPlan(PlanRequest.CreateDTO createListDTO);

    PlanResponse.DetailResultDTO getPlan(Integer userId, Integer planId);
}
