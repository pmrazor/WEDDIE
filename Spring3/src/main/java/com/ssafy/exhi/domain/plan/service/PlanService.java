package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse.SimpleResultDTO;

import java.util.List;

public interface PlanService {
    List<SimpleResultDTO> createPlan(PlanRequest.CreateListDTO createListDTO);
}
