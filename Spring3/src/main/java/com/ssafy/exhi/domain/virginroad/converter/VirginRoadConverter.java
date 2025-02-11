package com.ssafy.exhi.domain.virginroad.converter;

import com.ssafy.exhi.domain.couple.converter.CoupleConverter;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;

import java.util.List;

public class VirginRoadConverter {

    public static VirginRoad toEntity(VirginRoadRequest.CreateDTO dto, Couple couple) {

        List<Plan> plans = PlanConverter.toEntity(dto.getPlans());
        return VirginRoad.builder()
                .id(dto.getUserId())
                .couple(couple)
                .plans(plans)
                .virginRoadStatus(PlanStatus.IN_PROGRESS)
                .build();
    }

    public static VirginRoadResponse.SimpleResultDTO toDTO(VirginRoad virginRoad) {
        CoupleResponse.SimpleResultDTO couple = CoupleConverter.toSimpleDTO(virginRoad.getCouple());
        List<PlanResponse.SimpleResultDTO> plans = PlanConverter.toDTO(virginRoad.getPlans());

        return VirginRoadResponse.SimpleResultDTO.builder()
                .id(virginRoad.getId())
                .virginRoadStatus(virginRoad.getVirginRoadStatus())
                .couple(couple)
                .plans(plans)
                .build();
    }
}
