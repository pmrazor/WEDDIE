package com.ssafy.exhi.domain.recommendation.converter;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.converter.CoupleConverter;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;


import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;


public class RecommendationConverter {
    
    public static RecommendationResponse.RecommendedPlansDTO toRecommendedPlansDTO(
        VirginRoad virginRoad,
        Map<ServiceType, int[]> planWeights) {
                
        return RecommendationResponse.RecommendedPlansDTO.builder()
                .id(virginRoad.getId())
                .couple(CoupleConverter.toSimpleDTO(virginRoad.getCouple()))
                .plans(convertToRecommendedPlans(virginRoad.getPlans(), planWeights))
                .build();
    }

    private static List<PlanResponse.SimpleResultDTO> convertToRecommendedPlans(
            List<Plan> plans, Map<ServiceType, int[]> planWeights) {
        return plans.stream()
                .map(plan -> PlanConverter.toDTO(plan))
                .map(simpleDTO -> PlanResponse.SimpleResultDTO.builder()
                        .planId(simpleDTO.getPlanId())
                        .serviceType(simpleDTO.getServiceType())
                        .planStatus(simpleDTO.getPlanStatus())
                        .visible(planWeights.containsKey(simpleDTO.getServiceType()) ?
                                planWeights.get(simpleDTO.getServiceType())[0] == 1 : false)
                        .build())
                .collect(Collectors.toList());
    }
} 