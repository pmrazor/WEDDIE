package com.ssafy.exhi.domain.virginroad.model.dto;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.exception.ExceptionHandler;
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

        public void validateNoDuplicateServiceTypes() {
            long uniqueCount = plans.stream()
                    .map(PlanRequest.CreateDTO::getServiceType)
                    .distinct()
                    .count();

            if (uniqueCount < plans.size()) {
                throw new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_SERVICE_TYPE_NOT_VALID);
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private Integer userId;
        private List<PlanRequest.UpdateDTO> plans = new ArrayList<>();

        public void validateNoDuplicateServiceTypes() {
            long uniqueCount = plans.stream()
                    .map(PlanRequest.UpdateDTO::getServiceType)
                    .distinct()
                    .count();

            if (uniqueCount < plans.size()) {
                throw new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_SERVICE_TYPE_NOT_VALID);
            }
        }
    }
}
