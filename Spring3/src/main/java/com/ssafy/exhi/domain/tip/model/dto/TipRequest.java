package com.ssafy.exhi.domain.tip.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TipRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {

        @NotBlank
        private String tipContent;

        @NotNull
        private ServiceType serviceType;

    }
}
