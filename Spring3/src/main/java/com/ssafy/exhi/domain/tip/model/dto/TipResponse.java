package com.ssafy.exhi.domain.tip.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TipResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        private Integer tipId;

        private ServiceType serviceType;

        private Gender gender;

        private String tip;
    }
}
