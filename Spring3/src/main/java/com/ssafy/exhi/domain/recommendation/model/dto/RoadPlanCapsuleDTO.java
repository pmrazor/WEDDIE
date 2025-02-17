package com.ssafy.exhi.domain.recommendation.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoadPlanCapsuleDTO {
    private final Integer coupleId;              // 커플 식별자
    private final Integer virginRoadId;          // VirginRoad 식별자
    private final PlanStatus virginRoadStatus;   // VirginRoad 상태
    private final Integer planId;                // Plan 식별자
    private final ServiceType serviceType;       // 서비스 타입
    private final PlanStatus planStatus;         // Plan 상태
    private final Integer timeCapsuleId;         // TimeCapsule 식별자
    private final Integer planGrade;             // 긍정/부정 평가
}