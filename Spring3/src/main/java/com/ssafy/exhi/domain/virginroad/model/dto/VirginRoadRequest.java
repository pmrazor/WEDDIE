package com.ssafy.exhi.domain.virginroad.model.dto;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

public class VirginRoadRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        private Integer userId;
        private List<PlanRequest.CreateDTO> plans = new ArrayList<>();

        public void validateNoDuplicateServiceTypes() {
            long uniqueCount = plans.stream()
                    .map(PlanRequest.CreateDTO::getServiceType)
                    .distinct()
                    .count();

            if (uniqueCount < plans.size()) {
                throw new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_SERVICE_TYPE_NOT_VALID);
            }
        }

        public void sortPlan() {
            plans.sort(
                    Comparator.comparingInt(plan -> plan.getServiceType().ordinal())
            );
        }

        public boolean hasServiceType(ServiceType serviceType) {
            return plans.stream()
                    .anyMatch(plan -> plan.getServiceType() == serviceType);
        }

        public void addPlan(PlanRequest.CreateDTO plan) {
            plans.add(plan);
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
