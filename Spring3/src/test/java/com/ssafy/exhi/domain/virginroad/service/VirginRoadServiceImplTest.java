package com.ssafy.exhi.domain.virginroad.service;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VirginRoadServiceImplTest {

    @Mock
    private CoupleRepository coupleRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private VirginRoadRepository virginRoadRepository;
    @InjectMocks
    private VirginRoadServiceImpl virginRoadService;

    private Couple couple;
    private VirginRoad virginRoad;
    private Plan plan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 공통으로 사용할 테스트 데이터 생성
        couple = createTestCouple();
        plan = createTestPlan();
        virginRoad = createTestVirginRoad(couple, plan);

        // 기본 Mock 설정
        when(coupleRepository.findCoupleByUserId(1)).thenReturn(Optional.of(couple));
        when(virginRoadRepository.getVirginRoadByCouple(couple)).thenReturn(Optional.of(virginRoad));
    }

    @Test
    @DisplayName("버진로드 업데이트")
    void updateVirginRoad_성공() {
        // Given
        VirginRoadRequest.UpdateDTO updateDTO = createUpdateDTO();

        // When
        VirginRoadResponse.SimpleResultDTO result = virginRoadService.updateVirginRoad(updateDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    @DisplayName("버진로드 생성")
    void createVirginRoad_성공() {
        // Given
        VirginRoadRequest.CreateDTO createDTO = createCreateDTO();
        when(virginRoadRepository.save(any())).thenReturn(virginRoad);

        // When
        VirginRoadResponse.SimpleResultDTO result = virginRoadService.createVirginRoad(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    @DisplayName("버진로드는_항상_서비스타입과_갯수가_일치한다")
    void 버진로드는_항상_서비스타입과_갯수가_일치한다() {
        // When
        virginRoad.setDefaultPlans();

        // Then
        assertEquals(ServiceType.values().length, virginRoad.getPlans().size());
    }

    @Test
    @DisplayName("버진로드 삭제")
    void deleteVirginRoad_성공() {
        // When
        virginRoadService.deleteVirginRoad(1);

        // Then
        verify(virginRoadRepository).delete(any());
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private Couple createTestCouple() {
        User male = User.builder().id(1).build();
        User female = User.builder().id(2).build();
        return Couple.builder()
                .id(1)
                .male(male)
                .female(female)
                .build();
    }

    private Plan createTestPlan() {
        return Plan.builder()
                .id(1)
                .serviceType(ServiceType.DRESS_SHOP)
                .visible(false)
                .build();
    }

    private VirginRoad createTestVirginRoad(Couple couple, Plan plan) {
        List<Plan> plans = new ArrayList<>();
        plans.add(plan);

        return VirginRoad.builder()
                .id(1)
                .couple(couple)
                .plans(plans)
                .build();
    }

    private VirginRoadRequest.UpdateDTO createUpdateDTO() {
        List<PlanRequest.UpdateDTO> plans = new ArrayList<>();
        plans.add(
                PlanRequest.UpdateDTO.builder()
                        .planId(1)
                        .serviceType(ServiceType.DRESS_SHOP)
                        .visible(true)
                        .build()
        );

        return VirginRoadRequest.UpdateDTO.builder()
                .userId(1)
                .plans(plans)
                .build();
    }

    private VirginRoadRequest.CreateDTO createCreateDTO() {
        List<PlanRequest.CreateDTO> plans = new ArrayList<>();
        plans.add(
                PlanRequest.CreateDTO.builder()
                .serviceType(ServiceType.DRESS_SHOP)
                .build()
        );
        return VirginRoadRequest.CreateDTO.builder()
                .userId(1)
                .plans(plans)
                .build();
    }
}