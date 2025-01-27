package com.ssafy.exhi.domain.virginroad.converter;

import com.ssafy.exhi.domain.couple.converter.CoupleConverter;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;

import java.util.List;

public class VirginRoadConverter {
    public static VirginRoad toEntity(List<Plan> plans, Couple couple) {
        return VirginRoad.builder()
                .couple(couple)
                .plans(plans)
                .build()
                .addPlans(plans);
    }

    public static VirginRoadResponse.SimpleResultDTO toDTO(VirginRoad virginRoad) {
        CoupleResponse.SimpleResultDTO couple = CoupleConverter.toDTO(virginRoad.getCouple());
        List<PlanResponse.SimpleResultDTO> plans = PlanConverter.toDTO(virginRoad.getPlans());

        return VirginRoadResponse.SimpleResultDTO.builder()
                .id(virginRoad.getId())
                .couple(couple)
                .plans(plans)
                .build();
    }
}
