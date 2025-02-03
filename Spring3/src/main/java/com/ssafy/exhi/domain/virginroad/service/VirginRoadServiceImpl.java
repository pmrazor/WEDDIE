package com.ssafy.exhi.domain.virginroad.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import com.ssafy.exhi.domain.virginroad.converter.VirginRoadConverter;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VirginRoadServiceImpl implements VirginRoadService {

    private final VirginRoadRepository virginRoadRepository;
    private final CoupleRepository coupleRepository;
    private final DefaultTodoRepository defaultTodoRepository;

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

        VirginRoad virginRoad = VirginRoadConverter.toEntity(createDTO, couple);

        Map<ServiceType, List<DefaultTodo>> defaultTodos = defaultTodoRepository.findAll().stream().collect(Collectors.groupingBy(DefaultTodo::getServiceType));
        log.debug("default todos -> {}", defaultTodos);
        virginRoad.setDefaultPlans(defaultTodos);


        // 버진로드 저장
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

        // 2. 업데이트
        for (PlanRequest.UpdateDTO planDTO : updateDTO.getPlans()) {
            Plan plan = virginRoad.findPlanByServiceType(planDTO.getServiceType());
            plan.updateVisibility(planDTO);
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
