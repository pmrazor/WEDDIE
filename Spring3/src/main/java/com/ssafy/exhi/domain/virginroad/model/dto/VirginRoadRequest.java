package com.ssafy.exhi.domain.virginroad.model.dto;

import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class VirginRoadRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        private Integer userId;
        private List<PlanRequest.CreateDTO> plans = new ArrayList<>();

        public boolean isEmpty() {
            return plans == null || plans.isEmpty();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private Integer userId;
        private List<PlanRequest.UpdateDTO> plans = new ArrayList<>();

        public boolean isEmpty() {
            return plans == null || plans.isEmpty();
        }
    }
}
