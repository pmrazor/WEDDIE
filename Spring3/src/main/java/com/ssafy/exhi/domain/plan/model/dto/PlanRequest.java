package com.ssafy.exhi.domain.plan.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PlanRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateListDTO {
        private Integer userId;

        private List<CreateDTO> createDTO;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        private Integer userId;

        @Size(max = 20, message = "20자 이상 입력 불가능")
        private String shopName;

        private ServiceType serviceType;

        private boolean visible;

        private LocalDateTime planTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private Integer userId;

        private Integer planId;

        private String shopName;

        private ServiceType serviceType;

        private boolean visible;

        private LocalDateTime planTime;
    }
}
