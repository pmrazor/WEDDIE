package com.ssafy.exhi.domain.plan.converter;

import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.*;

public class PlanConverter {
    public static List<SimpleResultDTO> toDTO(List<Plan> plans) {
        return plans.stream().map(PlanConverter::toDTO).collect(Collectors.toList());
    }

    public static SimpleResultDTO toDTO(Plan plan) {
        return SimpleResultDTO.builder()
                .planId(plan.getId())
                .planStatus(plan.getPlanStatus())
                .serviceType(plan.getServiceType())
                .visible(plan.isVisible())
                .build();
    }

    public static Plan toEntity(PlanRequest.CreateDTO createDTO) {
        return Plan.builder()
                .shopName(createDTO.getShopName())
                .serviceType(createDTO.getServiceType())
                .visible(createDTO.isVisible())
                .planTime(createDTO.getPlanTime())
                .planStatus(PlanStatus.BEFORE_START)
                .build();
    }

    public static List<Plan> toEntity(List<PlanRequest.CreateDTO> plans) {
        return plans.stream()
                .map(PlanConverter::toEntity)
                .collect(Collectors.toList());
    }
}
