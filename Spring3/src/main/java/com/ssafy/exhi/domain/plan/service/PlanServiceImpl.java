package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
import com.ssafy.exhi.domain.tip.repository.TipRepository;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus; // enum 임포트
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
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

    @Override
    public AllResultDTO getAllPlans(String header) {
        Integer userId = jwtUtil.getUserId(header);

        // userId를 기반으로 coupleId를 찾기
        Couple couple = coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
        Integer coupleId = couple.getId();  // coupleId를 가져옴

        List<Plan> plans = planRepository.findByVirginRoadCoupleId(coupleId);
        if (plans.isEmpty()) {
            throw new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND);
        }

        // TimeWrapper로 currentDateTime을 얻어 플랜들에 대한 상태 및 날짜 계산
        for (Plan plan : plans) {
            plan.calculateStartAndEndDates();
            plan.updatePlanStatus(timeWrapper.now());  // TimeWrapper를 통해 현재 시간 설정

            // assignTipToPlan 메서드 호출 - Plan과 Tip 연결
            assignTipToPlan(plan.getId()); // 여기에서 Plan에 Tip을 할당하는 메서드 호출
        }

        Map<PlanStatus, List<PlanResponse.PlanDTO>> groupedPlans = groupPlansByStatus(plans);
        return new AllResultDTO(groupedPlans);
    }


    // Tip을 할당하는 메서드
    public void assignTipToPlan(Integer planId) {
        Plan plan = planRepository.findPlanById(planId).orElseThrow(() -> new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND));

        // Tip을 찾기
        Tip tip = tipRepository.findByServiceType(plan.getServiceType())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.TIP_NOT_FOUND));

        // Plan에 Tip 설정
        plan.setTip(tip);

        // Plan 저장
        planRepository.save(plan);
    }

    // 여기 문제 있나..?
    private Map<PlanStatus, List<PlanResponse.PlanDTO>> groupPlansByStatus(List<Plan> plans) {
        Map<PlanStatus, List<PlanResponse.PlanDTO>> groupedPlans = new HashMap<>();
        for (Plan plan : plans) {
            PlanStatus status = plan.getPlanStatus();

// TipDTO 생성 (null 체크 추가)
            PlanResponse.TipDTO tipDTO;

            if (plan.getTip() != null) {
                tipDTO = new PlanResponse.TipDTO(
                        plan.getTip().getId(),
                        plan.getTip().getTip()  // Tip이 있을 경우 getTip() 사용
                );
            } else {
                // Tip이 없을 경우에도 TipDTO 객체 생성
                tipDTO = new PlanResponse.TipDTO(
                        null,            // tipId가 없으면 null
                        "정보가 없습니다"  // tipContent가 없으면 기본 메시지
                );
            }

            if (plan.getTip() != null) {
                tipDTO = new PlanResponse.TipDTO(
                        plan.getTip().getId(),
                        plan.getTip().getTip()  // Tip이 있을 경우 getTip() 사용
                );
            }

            // PlanDTO 생성
            PlanResponse.PlanDTO planDTO = new PlanResponse.PlanDTO(
                    plan.getId(),
                    plan.getShopName(),
                    plan.getServiceType(),
                    tipDTO  // TipDTO 변환 (null일 수 있음)
            );

            groupedPlans.computeIfAbsent(status, k -> new ArrayList<>()).add(planDTO);

            // 상태별로 어떤 플랜들이 그룹화되고 있는지 로그로 확인
            log.info("Plan ID: {} - Grouped under Status: {}", plan.getId(), status);
        }
        // 상태별 플랜 그룹화 후 로그 추가
        log.info("Grouped Plans by Status: {}", groupedPlans);
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

