package com.ssafy.exhi.domain.timecapsule.converter;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleRequest;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleResponse;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import java.util.List;

public class TimeCapsuleConverter {
    public static TimeCapsule toEntity (
            TimeCapsuleRequest.CreateDTO request,
            Plan plan,
            PlanStatus timeCapsuleStatus
    ) {
        return TimeCapsule.builder()
                .userId(request.getUserId())
                .coupleId(request.getCoupleId())
                .plan(plan)
                .goodContent(request.getGoodContent())
                .goodImage(request.getGoodImage())
                .badContent(request.getBadContent())
                .badImage(request.getBadImage())
                .timeCapsuleStatus(timeCapsuleStatus)
                .planGrade(request.getPlanGrade())
                .build();
    }

    public static TimeCapsuleResponse.TimeCapsuleResponseDto toResponse (TimeCapsule timeCapsule) {
        return TimeCapsuleResponse.TimeCapsuleResponseDto.builder()
                .timeCapsuleId(timeCapsule.getId())
                .planId(timeCapsule.getPlan().getId())
                .userId(timeCapsule.getUserId())
                .goodContent(timeCapsule.getGoodContent())
                .goodImage(timeCapsule.getGoodImagePath())
                .badContent(timeCapsule.getBadContent())
                .badImage(timeCapsule.getGoodImagePath())
                .planGrade(timeCapsule.getPlanGrade())
                .build();
    }

    public static List<TimeCapsuleResponse.TimeCapsuleResponseDto> toResponse (List<TimeCapsule> timeCapsules) {
        return timeCapsules.stream()
                .map(TimeCapsuleConverter::toResponse)
                .toList();
    }
}
