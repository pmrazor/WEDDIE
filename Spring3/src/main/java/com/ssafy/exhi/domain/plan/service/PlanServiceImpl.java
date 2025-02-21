package com.ssafy.exhi.domain.plan.service;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.AllResultDTO;
import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.DetailResultDTO;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.TimeWrapper;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
import com.ssafy.exhi.domain.tip.repository.TipRepository;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final TipRepository tipRepository;
    private final PlanRepository planRepository;
    private final CoupleRepository coupleRepository;
    private final UserDetailRepository userDetailRepository;
    private final TimeWrapper timeWrapper;

    @Override
    public DetailResultDTO getPlan(Integer userId, Integer planId) {
        Integer coupleId = coupleRepository.findCoupleIdByUserId(userId);
        Gender gender = findGenderByUserId(userId);

        TodoType type = gender.equals(Gender.FEMALE) ? TodoType.FEMALE : TodoType.MALE;
        List<TodoType> list = Arrays.asList(TodoType.COMMON, type);

        Plan plan = planRepository.findPlanByIdWithFilteredTodos(planId, list);

        checkPlanOwnerShip(coupleId, plan);

        return PlanConverter.toDetailDTO(plan);
    }

    @Override
    public AllResultDTO getAllPlans(Integer userId) {

        Couple couple = coupleRepository.findCoupleByUserId(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));

        List<Plan> plans = planRepository.findByVirginRoadCoupleId(couple.getId());
        if (plans.isEmpty()) {
            throw new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND);
        }

        for (Plan plan : plans) {
            // TimeWrapper로 currentDateTime을 얻어 플랜들에 대한 상태 및 날짜 계산
            // assignTipToPlan 메서드 호출 - Plan과 Tip 연결
            assignTipToPlan(plan); // 여기에서 Plan에 Tip을 할당하는 메서드 호출
        }
        Map<PlanStatus, List<PlanResponse.PlanDTO>> groupedPlans = groupPlansByStatus(plans);

        return PlanConverter.toResultDTO(
                timeWrapper.now().toLocalDate(),
                couple.getLoveAnniversary(),
                couple.getMarriageDate(),
                groupedPlans
        );
    }

    // Tip을 할당하는 메서드
    public void assignTipToPlan(Plan plan) {
        // Tip을 찾기
        Tip tip = tipRepository.findTipsByServiceType(plan.getServiceType())
            .stream()
            .findFirst()
            .orElse(null);  // Tip이 없으면 null로 설정

        // Plan에 Tip 설정
        plan.setTip(tip == null ? null : tip.getTipContent());

        // Plan 저장
        planRepository.save(plan);
    }

    // 여기 문제 있나..?
    private Map<PlanStatus, List<PlanResponse.PlanDTO>> groupPlansByStatus(List<Plan> plans) {
        Map<PlanStatus, List<PlanResponse.PlanDTO>> groupedPlans = new HashMap<>();
        for (Plan plan : plans) {
            PlanStatus status = plan.getPlanStatus();

            // PlanDTO 생성
            PlanResponse.PlanDTO planDTO = PlanResponse.PlanDTO.builder()
                    .planId(plan.getId())
                    .shopName(plan.getShopName())
                    .serviceType(plan.getServiceType())
                    .tip(plan.getTip())
                    .visible(plan.isVisible())
                    .planTime(plan.getPlanTime().toLocalDate())
                    .build();

            groupedPlans.computeIfAbsent(status, k -> new ArrayList<>()).add(planDTO);

            // 상태별로 어떤 플랜들이 그룹화되고 있는지 로그로 확인
            log.info("Plan ID: {} - Grouped under Status: {}", plan.getId(), status);
        }
        // 상태별 플랜 그룹화 후 로그 추가
        log.info("Grouped Plans by Status: {}", groupedPlans);
        return groupedPlans;
    }

    private Gender findGenderByUserId(Integer userId) {
        Gender gender = userDetailRepository.findByUserId(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND))
            .getGender();
        return gender;
    }

    private void checkPlanOwnerShip(Integer coupleId, Plan plan) {
        Integer planCoupleId = Optional.ofNullable(plan)
            .map(Plan::getVirginRoad)
            .map(VirginRoad::getCouple)
            .map(Couple::getId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus._FORBIDDEN));

        if (!Objects.equals(coupleId, planCoupleId)) {
            throw new ExceptionHandler(ErrorStatus._FORBIDDEN);
        }
    }

    @Override
    public void allPlanUpdate(TimeWrapper timeWrapper) {
        List<Plan> plans = planRepository.findPlansByPlanStatusNot(PlanStatus.FINISHED);

        for (Plan plan : plans) {
            plan.updatePlanStatus(timeWrapper.now());
        }
    }
}

