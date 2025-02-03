package com.ssafy.exhi.domain.timecapsule.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.timecapsule.converter.TimeCapsuleConverter;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleRequest;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleResponse;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import com.ssafy.exhi.domain.timecapsule.repository.TimeCapsuleRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
@RequiredArgsConstructor
public class TimeCapsuleServiceImpl implements TimeCapsuleService {

    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final CoupleRepository coupleRepository;

    @Override
    public TimeCapsuleResponse.TimeCapsuleResponseDto createTimeCapsule(TimeCapsuleRequest.CreateDTO registTimeCapsule) {
        User user = userRepository.findUserById(registTimeCapsule.getUserId()).
                orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));

        Plan plan = planRepository.findById(Long.valueOf(registTimeCapsule.getPlanId()))
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND));

        // 타임캡슐을 작성할 단계가 아닌 경우
        if (plan.getPlanStatus() != PlanStatus.NOT_TIME_CAPSULE) {
            throw new ExceptionHandler(ErrorStatus.INVALID_PLAN_STATUS);
        }

        // 커플 아이디 세팅
        Couple couple = coupleRepository.findCoupleByUserId(user.getId())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));

        registTimeCapsule.setCoupleId(couple.getId());

        PlanStatus planStatus;

        // 이미 해당 유저가 해당 플랜에 작성한 타임캡슐이 있다면
        if(timeCapsuleRepository.existsByUserIdAndPlanId(registTimeCapsule.getUserId(), registTimeCapsule.getPlanId())) {
            throw new ExceptionHandler(ErrorStatus.TIMECAPSULE_ALREADY_EXIST);
        }
        else {
            // 나 개인이 볼 타임캡슐 스테이터스 FINISHED
            planStatus = PlanStatus.FINISHED;
        }

        TimeCapsule timeCapsule = TimeCapsuleConverter.toEntity(registTimeCapsule, plan, planStatus);
        TimeCapsule savedTimeCapsule = timeCapsuleRepository.save(timeCapsule);

        // 플랜 상태 업데이트
        plan.getTimeCapsules().add(savedTimeCapsule);
        plan.updateTimeCapsuleStatus();
        planRepository.save(plan);

        return TimeCapsuleConverter.toResponse(savedTimeCapsule);
    }

    // 커플 당 하나의 버진로드 가지니까 커플 아이디로 조회한 모든 캡슐 가져오면 되겠지?
    @Override
    public List<TimeCapsuleResponse.TimeCapsuleResponseDto> findTimeCapsulesByCoupleId(Integer userId) {
        User user = userRepository.findUserById(userId).
                orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));

        Couple couple = coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));

        List<TimeCapsule> timeCapsules = timeCapsuleRepository.findAllByCoupleId(couple.getId());

        // Collection 인 List 를 Stream(데이터 처리 파이프라인 으로 변환)
        return timeCapsules.stream()
                // map() 으로 스트림의 각 요소를 다른 형태로 변환
                .map(TimeCapsuleConverter::toResponse)
                // 스트림 처리 결과를 다시 List 형태로 수집
                .collect(Collectors.toList());
    }
}
