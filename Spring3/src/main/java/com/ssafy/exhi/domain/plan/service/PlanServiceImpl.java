package com.ssafy.exhi.domain.plan.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.DetailResultDTO;
import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.SimpleResultDTO;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;

    @Override
    public SimpleResultDTO createPlan(PlanRequest.CreateDTO createDTO) {

        Plan plan = PlanConverter.toEntity(createDTO);
        return PlanConverter.toDTO(planRepository.save(plan));
    }

    @Override
    public DetailResultDTO getPlan(Integer userId, Integer planId) {
        Couple couple = getCoupleByUserId(userId);
        Plan plan = getPlanById(planId);
        checkPlanOwnerShip(couple, plan);

        return PlanConverter.toDetailDTO(plan);
    }

    private void checkPlanOwnerShip(Couple couple , Plan plan) {
        Integer coupleId = Optional.ofNullable(plan)
                .map(Plan::getVirginRoad)
                .map(VirginRoad::getCouple)
                .map(Couple::getId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus._FORBIDDEN));

        if (!Objects.equals(couple.getId(), coupleId)) {
            throw new ExceptionHandler(ErrorStatus._FORBIDDEN);
        }
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
        );
    }

    private Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND)
        );
    }

    private Plan getPlanById(Integer planId) {
        return planRepository.findPlanById(planId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND)
        );
    }

}
