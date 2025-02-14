package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.DetailResultDTO;
import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.AllResultDTO;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final CoupleRepository coupleRepository;
    private final UserDetailRepository userDetailRepository;

    @Override
    public DetailResultDTO getPlan(Integer userId, Integer planId) {
        Couple couple = getCoupleByUserId(userId);
        Gender gender = findGenderByUserId(userId);
        
        TodoType type = gender.equals(Gender.FEMALE) ? TodoType.FEMALE : TodoType.MALE;
        List<TodoType> list = Arrays.asList(TodoType.COMMON, type);

        Plan plan = planRepository.findPlanByIdWithFilteredTodos(planId, list);

        checkPlanOwnerShip(couple, plan);

        return PlanConverter.toDetailDTO(plan);
    }

    private static final int DURATION = 30;  // 상수 DURATION 30일 설정

    // 유저 ID에 맞는 플랜을 가져오는 메서드
    @Override
    public AllResultDTO getAllPlans(Integer coupleId) {
        // coupleId에 해당하는 모든 플랜을 가져옴
        List<Plan> plans = planRepository.findByVirginRoad_Couple_Id(coupleId);  // VirginRoad -> Couple -> coupleId로 플랜 조회
        if (plans.isEmpty()) {
            throw new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND);
        }

        // 플랜의 날짜 계산 메서드 호출 (각 플랜에 대해 startTime, endTime 계산)
        for (Plan plan : plans) {
            plan.calculateStartAndEndDates(DURATION);  // 플랜마다 시작일과 종료일을 계산
            plan.updatePlanStatus(timeWrapper.now(), DURATION);  // 상태 업데이트
        }

        // 플랜을 상태별로 그룹화
        Map<PlanStatus, List<Integer>> groupedPlans = groupPlansByStatus(plans);

        // 사용자 플랜 응답 DTO 생성
        return new AllResultDTO(groupedPlans);
    }

    // 상태별로 플랜을 그룹화하는 메서드
    private Map<PlanStatus, List<Integer>> groupPlansByStatus(List<Plan> plans) {
        Map<PlanStatus, List<Integer>> groupedPlans = new HashMap<>();
        for (Plan plan : plans) {
            // 상태별로 플랜 ID를 그룹에 추가
            groupedPlans.computeIfAbsent(plan.getPlanStatus(), k -> new ArrayList<>()).add(plan.getId());
        }
        return groupedPlans;
    }





    private Gender findGenderByUserId(Integer userId) {
        Gender gender = userDetailRepository.findByUser_Id(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND))
            .getGender();
        return gender;
    }

    private void checkPlanOwnerShip(Couple couple, Plan plan) {
        Integer coupleId = Optional.ofNullable(plan)
            .map(Plan::getVirginRoad)
            .map(VirginRoad::getCouple)
            .map(Couple::getId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus._FORBIDDEN));

        if (!Objects.equals(couple.getId(), coupleId)) {
            throw new ExceptionHandler(ErrorStatus._FORBIDDEN);
        }
    }

    private Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId).orElseThrow(
            () -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND)
        );
    }
}
