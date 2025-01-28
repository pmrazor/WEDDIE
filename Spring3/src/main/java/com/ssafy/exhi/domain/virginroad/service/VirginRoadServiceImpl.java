package com.ssafy.exhi.domain.virginroad.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.virginroad.converter.VirginRoadConverter;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
     * 버진로드가 비어있는 값은 디폴트로 생성 (visible이 false로 설정)
     * @param createDTO
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO createVirginRoad(VirginRoadRequest.CreateDTO createDTO) {
        Couple couple = getCoupleByUserId(createDTO.getUserId());

        existVirginRoad(couple);
        setDefaultPlans(createDTO);

        // 플랜을 저장
        List<Plan> plans = PlanConverter.toEntity(createDTO.getPlans());
        planRepository.saveAll(plans);

        // 버진로드를 저장
        VirginRoad virginRoad = VirginRoadConverter.toEntity(plans, couple);
        VirginRoad saved = virginRoadRepository.save(virginRoad);

        return VirginRoadConverter.toDTO(saved);
    }

    private static void setDefaultPlans(VirginRoadRequest.CreateDTO createDTO) {
        createDTO.sortPlan();
        for (ServiceType serviceType : ServiceType.values()) {
            boolean exists = createDTO.hasServiceType(serviceType);

            if (!exists) {
                PlanRequest.CreateDTO defaultPlan = PlanRequest.CreateDTO.createDefaultDTO(serviceType);
                createDTO.addPlan(defaultPlan);
            }
        }
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
     * 버진로드의 계획들을 모두 업데이트
     *
     * @param updateDTO
     * @return
     */
    @Override
    public VirginRoadResponse.SimpleResultDTO updateVirginRoad(VirginRoadRequest.UpdateDTO updateDTO) {
        // 1. 사용자의 버진로드를 들고오기
        Couple couple = getCoupleByUserId(updateDTO.getUserId());
        VirginRoad virginRoad = getVirginRoadByCouple(couple);

        // 2. 버진로드의 서비스 타입을 일치 시키기
        virginRoad.getPlans().sort(Comparator.comparingInt(plan -> plan.getServiceType().ordinal()));
        updateDTO.getPlans().sort(Comparator.comparingInt(plan -> plan.getServiceType().ordinal()));

        // 3. 각 Plan 엔티티 업데이트
        for (int i = 0; i < virginRoad.getPlans().size(); i++) {
            Plan plan = virginRoad.getPlans().get(i);
            PlanRequest.UpdateDTO dto = updateDTO.getPlans().get(i);

            plan.updateVisibility(dto);
        }

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
