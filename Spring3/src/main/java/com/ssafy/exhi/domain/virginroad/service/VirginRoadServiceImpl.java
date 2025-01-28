package com.ssafy.exhi.domain.virginroad.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
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
     *
     * @param createDTO
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO createVirginRoad(VirginRoadRequest.CreateDTO createDTO) {
        Couple couple = getCoupleByUserId(createDTO.getUserId());
        existVirginRoad(couple);
        // 플랜을 저장
        List<Plan> plans = PlanConverter.toEntity(createDTO.getPlans());
        planRepository.saveAll(plans);

        // 버진로드를 저장
        VirginRoad virginRoad = VirginRoadConverter.toEntity(plans, couple);
        VirginRoad saved = virginRoadRepository.save(virginRoad);

        return VirginRoadConverter.toDTO(saved);

    }

    /**
     * 버진로드를 한번에 보여준다.
     * 중요한 것은 모둔 결혼 단계의 상태를 보여주기
     * @param userId
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO getVirginRoad(Integer userId) {
        Couple couple = getCoupleByUserId(userId);
        VirginRoad virginRoad = getVirginRoadByCouple(couple);

        return VirginRoadConverter.toDTO(virginRoad);
    }

    /**
     * 버진로드를 삭제한다.
     * @param userId
     */
    @Override
    public void deleteVirginRoad(Integer userId) {
        Couple couple = getCoupleByUserId(userId);
        VirginRoad virginRoad = getVirginRoadByCouple(couple);

        virginRoadRepository.delete(virginRoad);
    }

    private Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND)
        );
    }

    private VirginRoad getVirginRoadByCouple(Couple couple) {

        return virginRoadRepository.getVirginRoadByCouple(couple).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_NOT_FOUND)
        );
    }

    private void existVirginRoad(Couple couple) {
        if (virginRoadRepository.existsVirginRoadByCouple(couple)) {
            throw new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_EXISTS);
        }
    }
}
