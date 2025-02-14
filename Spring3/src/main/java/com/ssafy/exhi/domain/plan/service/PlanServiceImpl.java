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
import com.ssafy.exhi.domain.tip.service.TipServiceImpl;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final CoupleRepository coupleRepository;
    private final UserDetailRepository userDetailRepository;
    private final TipServiceImpl tipService;  // TipService를 주입받음

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
