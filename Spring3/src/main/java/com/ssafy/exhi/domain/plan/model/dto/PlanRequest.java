package com.ssafy.exhi.domain.plan.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ssafy.exhi.domain.ServiceType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PlanRequest {

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

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime planTime;

        public static CreateDTO createDefaultDTO(ServiceType serviceType) {
            return PlanRequest.CreateDTO.builder()
                    .serviceType(serviceType)
                    .visible(false) // 디폴트 값 설정 (예: isVisible = false)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private Integer userId;

        private Integer planId;

        private ServiceType serviceType;

        private boolean visible;
    }
}
