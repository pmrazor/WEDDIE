package com.ssafy.exhi.domain.virginroad.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class VirginRoadResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResultDTO {
        private int id;

        private PlanStatus virginRoadStatus;

        private CoupleResponse.SimpleResultDTO couple;

        private List<PlanResponse.SimpleResultDTO> plans = new ArrayList<>();

        public PlanResponse.SimpleResultDTO findPlanByServiceType(ServiceType serviceType) {
            return plans.stream()
                    .filter(plan -> plan.getServiceType() == serviceType)
                    .findFirst()
                    .orElse(null);
        }
    }

}
