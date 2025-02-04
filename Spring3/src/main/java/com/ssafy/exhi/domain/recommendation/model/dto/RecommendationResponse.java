package com.ssafy.exhi.domain.recommendation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class RecommendationResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedPlansDTO {
        private Integer clusterId;
        private List<PlanDTO> plans;
        private Integer similarUsersCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanDTO {
        private Integer planId;
        private String title;
        private String description;
        private Integer estimatedBudget;
        private Double rating;
        private Integer completedCount;
    }
}
