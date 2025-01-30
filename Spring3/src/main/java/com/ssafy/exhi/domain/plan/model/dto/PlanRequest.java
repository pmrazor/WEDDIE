package com.ssafy.exhi.domain.plan.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ssafy.exhi.domain.ServiceType;
import jakarta.validation.constraints.NotNull;
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

        @NotNull(message = "서비스 타입은 필수 입니다.")
        private ServiceType serviceType;

        @NotNull(message = "가시성 타입은 필수 입니다.")
        private boolean visible;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime planTime;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private Integer userId;

        private ServiceType serviceType;

        private boolean visible;
    }
}
