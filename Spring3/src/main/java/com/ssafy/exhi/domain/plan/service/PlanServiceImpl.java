package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanConverter planConverter;

    @Override
    public List<SimpleResultDTO> createPlan(PlanRequest.CreateListDTO createListDTO) {
        List<Plan> plans = planConverter.toEntity(createListDTO);
        List<Plan> savedPlans = planRepository.saveAll(plans);

        return planConverter.toDTO(savedPlans);
    }
}
