package com.ssafy.exhi.domain.timecapsule.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TimeCapsuleResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeCapsuleResponseDto {
        private Integer timeCapsuleId;

        private Integer planId;

        private Integer userId;

        private String goodContent;

        private String goodImage;

        private String badContent;

        private String badImage;

        private Integer planGrade;
    }
}
