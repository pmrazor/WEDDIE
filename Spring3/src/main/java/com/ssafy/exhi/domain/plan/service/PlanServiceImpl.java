package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    public SimpleResultDTO createPlan(PlanRequest.CreateDTO createDTO) {
        Plan plan = PlanConverter.toEntity(createDTO);
        return PlanConverter.toDTO(planRepository.save(plan));
    }

}
