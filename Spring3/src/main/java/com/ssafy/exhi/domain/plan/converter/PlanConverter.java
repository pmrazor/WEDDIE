package com.ssafy.exhi.domain.plan.converter;

import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest.CreateListDTO;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.*;

@Component
public class PlanConverter {
    public List<SimpleResultDTO> toDTO(List<Plan> plans) {
        return plans.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public SimpleResultDTO toDTO(Plan plan) {
        return SimpleResultDTO.builder()
                .planId(plan.getId())
                .planStatus(plan.getPlanStatus())
                .serviceType(plan.getServiceType())
                .visible(plan.isVisible())
                .build();
    }

    public Plan toEntity(PlanRequest.CreateDTO createDTO) {
        return Plan.builder()
                .shopName(createDTO.getShopName())
                .serviceType(createDTO.getServiceType())
                .visible(createDTO.isVisible())
                .planTime(createDTO.getPlanTime())
                .planStatus(PlanStatus.BEFORE_START)
                .build();
    }

    public List<Plan> toEntity(CreateListDTO createListDTO) {
        return createListDTO.getCreateDTO().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
