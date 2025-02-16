package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
import com.ssafy.exhi.domain.tip.repository.TipRepository;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus; // enum 임포트
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.tip.model.dto.TipResponse;
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

    private final UserRepository userRepository;
    private final TipRepository tipRepository;
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

//        log.debug("User ID: {} - Gender: {}", userId, gender); // 성별 확인 로그 추가


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
            assignTipToPlan(plan, userId); // 여기에서 Plan에 Tip을 할당하는 메서드 호출
        }

        Map<PlanStatus, List<PlanResponse.PlanDTO>> groupedPlans = groupPlansByStatus(plans);
        return new AllResultDTO(groupedPlans);
    }


    // Tip을 할당하는 메서드
    public void assignTipToPlan(Plan plan, Integer userId) {
        // 사용자 성별을 findGenderByUserId로 가져옴
        Gender gender = findGenderByUserId(userId);  // 이미 구현된 메서드 사용
        log.debug("Assigning Tip for Plan ID: {} - User ID: {} with Gender: {}", plan.getId(), userId, gender); // 로그 추가
//        log.debug("Assigning Tip for Plan ID: {} - User ID: {} with Gender: {}", plan.getId(), userId, gender); // 로그 추가
        // Tip을 찾기 (성별 필터링은 TipRepository에서 처리)
        List<Tip> tips = tipRepository.findTipsByServiceTypeAndUserDetail_Gender(plan.getServiceType(), gender);
        log.debug("Found {} tips for Plan ID: {} with Service Type: {} and Gender: {}", tips.size(), plan.getId(), plan.getServiceType(), gender); // 추가된 로그

        // 첫 번째 Tip을 선택 (없으면 null)
        Tip tip = tips.stream()
                .findFirst()
                .orElse(null);  // Tip이 없으면 null로 설정

        // Plan에 Tip 설정
        plan.setTip(tip);

        // Plan 저장
        planRepository.save(plan);
    }

    private Map<PlanStatus, List<PlanResponse.PlanDTO>> groupPlansByStatus(List<Plan> plans) {
        Map<PlanStatus, List<PlanResponse.PlanDTO>> groupedPlans = new HashMap<>();

        for (Plan plan : plans) {
            PlanStatus status = plan.getPlanStatus();

            // Tip 정보를 TipDTO 객체로 변환
            PlanResponse.TipDTO tipDTO;

            if (plan.getTip() != null) {
                tipDTO = new PlanResponse.TipDTO(
                        plan.getTip().getId(),
                        plan.getTip().getTipContent()
                );
                log.info("Tip found for Plan ID: {} - Tip Content: {}", plan.getId(), plan.getTip().getTipContent());
            } else {
                // Tip이 없을 경우에도 TipDTO 객체 생성
                tipDTO = new PlanResponse.TipDTO(
                        null,            // tipId가 없으면 null
                        "정보가 없습니다"  // tipContent가 없으면 기본 메시지
                );
                log.info("No Tip found for Plan ID: {}", plan.getId());
            }

            // PlanDTO 생성
            PlanResponse.PlanDTO planDTO = new PlanResponse.PlanDTO(
                    plan.getId(),
                    plan.getShopName(),
                    plan.getServiceType(),
                    tipDTO  // tip 객체 추가
            );

            groupedPlans.computeIfAbsent(status, k -> new ArrayList<>()).add(planDTO);

            // 상태별로 어떤 플랜들이 그룹화되고 있는지 로그로 확인
//            log.info("Plan ID: {} - Grouped under Status: {}", plan.getId(), status);
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

