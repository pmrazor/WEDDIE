package com.ssafy.exhi.domain.tip.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.tip.converter.TipConverter;
import com.ssafy.exhi.domain.tip.model.dto.TipRequest;
import com.ssafy.exhi.domain.tip.model.dto.TipResponse;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
import com.ssafy.exhi.domain.tip.repository.TipRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TipServiceImpl implements TipService {

    private final TipRepository tipRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Override
    public List<TipResponse.DetailResultDTO> getTips(Integer planId, Integer userId) {
        Plan plan = findPlanByPlanId(planId);
        User user = findUserByUserId(userId);
        ServiceType serviceType = plan.getServiceType();
        Gender gender = user.getUserDetail().getGender(); // 유저의 성별을 가져옴

        List<Tip> tips = tipRepository.findTipsByServiceTypeAndUserDetail_Gender(serviceType, gender);  // 성별에 맞는 팁 찾기

        return TipConverter.toDTO(tips);
    }

    @Override
    public TipResponse.DetailResultDTO createTip(TipRequest.CreateDTO createDTO) {
        Tip tip = TipConverter.toEntity(createDTO);
        tip = tipRepository.save(tip);

        return TipConverter.toDTO(tip);
    }

    private User findUserByUserId(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
            );
    }

    private Plan findPlanByPlanId(Integer planId) {
        return planRepository.findPlanById(planId).orElseThrow(
            () -> new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND)
        );
    }


}
