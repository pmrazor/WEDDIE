package com.ssafy.exhi.domain.virginroad.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.virginroad.converter.VirginRoadConverter;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VirginRoadServiceImpl implements VirginRoadService {

    private final VirginRoadRepository virginRoadRepository;
    private final CoupleRepository coupleRepository;
    private final PlanRepository planRepository;

    /**
     * 플랜과 버진로드를 동시에 생성하기
     * @param createDTO
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO createVirginRoad(VirginRoadRequest.CreateDTO createDTO) {
        Couple couple = getCoupleByUserId(createDTO.getUserId());

        // 플랜을 저장
        List<Plan> plans = PlanConverter.toEntity(createDTO.getPlans());
        planRepository.saveAll(plans);

        // 버진로드를 저장
        VirginRoad virginRoad = VirginRoadConverter.toEntity(plans, couple);

        VirginRoad saved = virginRoadRepository.save(virginRoad);

        return VirginRoadConverter.toDTO(saved);

    }

    private Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
        );
    }
}
