package com.ssafy.exhi.domain.plan.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PlanResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        private Integer planId;

        private String shopName;

        private ServiceType serviceType;

        private boolean visible;

        private LocalDateTime planTime;

        private PlanStatus planStatus;

        private List<TodoResponse.DetailResultDTO> todos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResultDTO {
        private Integer planId;

        private ServiceType serviceType;

        private boolean visible;

        private PlanStatus planStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllResultDTO {
        private LocalDate loveAnniversary;
        private LocalDate marriageDate;
        private LocalDate currentDate;
        private Map<PlanStatus, List<PlanDTO>> groupedPlans;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanDTO {
        private Integer planId;
        private String shopName;
        private ServiceType serviceType;
        private String tip;
        private boolean visible;
        private LocalDate planTime;
    }
}
