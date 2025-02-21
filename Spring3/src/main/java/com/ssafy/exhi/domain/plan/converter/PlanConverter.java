package com.ssafy.exhi.domain.plan.converter;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.SimpleResultDTO;

import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.todo.converter.TodoConverter;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static PlanRequest.CreateDTO toCreateDTO(Plan plan, LocalDate planDate) {
        return PlanRequest.CreateDTO.builder()
            .serviceType(plan.getServiceType())
            .shopName(plan.getShopName())
            .visible(plan.isVisible())
            .planTime(planDate.atStartOfDay())
            .build();
    }

    public static PlanResponse.DetailResultDTO toDetailDTO(Plan plan) {
        List<TodoResponse.DetailResultDTO> todos = TodoConverter.toDTO(plan.getTodos());

        return PlanResponse.DetailResultDTO.builder()
            .planId(plan.getId())
            .shopName(plan.getShopName())
            .planTime(plan.getPlanTime())
            .planStatus(plan.getPlanStatus())
            .serviceType(plan.getServiceType())
            .visible(plan.isVisible())
            .todos(todos)
            .build();
    }

    public static Plan toEntity(PlanRequest.CreateDTO createDTO) {
        return Plan.builder()
            .shopName(createDTO.getShopName())
            .serviceType(createDTO.getServiceType())
            .visible(createDTO.isVisible())
            .todos(new ArrayList<>())
            .planTime(createDTO.getPlanTime())
            .planStatus(PlanStatus.BEFORE_START)
            .build();
    }

    public static List<Plan> toEntity(List<PlanRequest.CreateDTO> plans) {
        return plans.stream()
            .map(PlanConverter::toEntity)
            .collect(Collectors.toList());
    }

    public static PlanResponse.AllResultDTO toResultDTO(
            LocalDate currentDate,
            LocalDate loveAnniversary,
            LocalDate marriageDate,
            Map<PlanStatus, List<PlanResponse.PlanDTO>> result
    ) {
        return PlanResponse.AllResultDTO.builder()
            .groupedPlans(result)
            .loveAnniversary(loveAnniversary)
                .currentDate(currentDate)
            .marriageDate(marriageDate)
            .build();
    }
}
