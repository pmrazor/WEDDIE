package com.ssafy.exhi.domain.recommendation.model.dto;

import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedPlansDTO {
        private Integer id;
        private CoupleResponse.SimpleResultDTO couple;
        private List<PlanResponse.SimpleResultDTO> plans = new ArrayList<>();
    }
}