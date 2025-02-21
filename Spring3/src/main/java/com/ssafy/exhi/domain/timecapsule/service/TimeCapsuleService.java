package com.ssafy.exhi.domain.timecapsule.service;

import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleRequest;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleResponse;

import java.util.List;

public interface TimeCapsuleService {
    TimeCapsuleResponse.TimeCapsuleResponseDto createTimeCapsule(TimeCapsuleRequest.CreateDTO timeCapsule);

    // 한 플랜에 두 명의 사용자가 타임캡슐을 적을 수도 있고, 둘 다 안 적었을수도 있음
    // 플랜에 아무 타임캡슐도 없으면 프론트엔드에 빈 값을 반환해줘야 함
    List<TimeCapsuleResponse.TimeCapsuleResponseDto> findTimeCapsulesByCoupleId(Integer userId);
}