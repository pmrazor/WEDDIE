package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus; // enum 임포트
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository; // 버진로드에서 커플ID 찾기
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.TimeWrapper;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.ssafy.exhi.util.JWTUtil;


import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
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
    private final TimeWrapper timeWrapper;
    private final VirginRoadRepository virginRoadRepository;
    private final JWTUtil jwtUtil;

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

    @Override
    public AllResultDTO getAllPlans(String header) {
        Integer coupleId = jwtUtil.getCoupleId(header);

        List<Plan> plans = planRepository.findByVirginRoad_Couple_Id(coupleId);
        if (plans.isEmpty()) {
            throw new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND);
        }

        // TimeWrapper로 currentDateTime을 얻어 플랜들에 대한 상태 및 날짜 계산
        for (Plan plan : plans) {
            plan.calculateStartAndEndDates(DURATION);
            plan.updatePlanStatus(timeWrapper.now(), DURATION);  // TimeWrapper를 통해 현재 시간 설정
        }

        Map<PlanStatus, List<Integer>> groupedPlans = groupPlansByStatus(plans);
        return new AllResultDTO(groupedPlans);
    }

//    // userId를 통해 coupleId를 가져오는 메서드
//    private Integer getCoupleIdByUserId(Integer userId) {
//        VirginRoad virginRoad =  virginRoadRepository.findByCouple_UserId(userId) // VirginRoadRepository에서 userId로 VirginRoad를 찾기
//                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
//        return virginRoad.getCouple().getId();  // coupleId 반환
//    }

    private Map<PlanStatus, List<Integer>> groupPlansByStatus(List<Plan> plans) {
        Map<PlanStatus, List<Integer>> groupedPlans = new HashMap<>();
        for (Plan plan : plans) {
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

