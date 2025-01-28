package com.ssafy.exhi.domain.virginroad.service;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.virginroad.controller.VirginRoadController;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VirginRoadServiceImplTest {

    @Mock
    private CoupleRepository coupleRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private VirginRoadRepository virginRoadRepository;

    @InjectMocks
    private VirginRoadServiceImpl virginRoadService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateVirginRoad() {
        // Given
        VirginRoadRequest.UpdateDTO updateDTO = new VirginRoadRequest.UpdateDTO();
        updateDTO.setUserId(1);
        updateDTO.setPlans(Arrays.asList(
                PlanRequest.UpdateDTO.builder()
                        .planId(1)
                        .serviceType(ServiceType.DRESS_SHOP)
                        .visible(true)
                        .build())
        );

        User male = User.builder().id(1).build();
        User female = User.builder().id(2).build();

        Couple couple = Couple.builder()
                .id(1)
                .female(female)
                .male(male)
                .build();

        Plan plan = Plan.builder()
                .id(1)
                .serviceType(ServiceType.DRESS_SHOP)
                .visible(false)
                .build();

        VirginRoad virginRoad = VirginRoad.builder()
                .id(1)
                .couple(couple)
                .plans(Arrays.asList(plan))
                .build();


        when(coupleRepository.findCoupleByUserId(updateDTO.getUserId())).thenReturn(Optional.of(couple));
        when(virginRoadRepository.getVirginRoadByCouple(couple)).thenReturn(Optional.of(virginRoad));

        // When
        VirginRoadResponse.SimpleResultDTO result = virginRoadService.updateVirginRoad(updateDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertTrue(result.getPlans().get(0).isVisible());
        verify(coupleRepository, times(1)).findCoupleByUserId(updateDTO.getUserId());
        verify(virginRoadRepository, times(1)).getVirginRoadByCouple(couple);
    }

    @Test
    public void testCreateVirginRoad() {
        // Given
        User male = User.builder().id(1).build();
        User female = User.builder().id(2).build();
        VirginRoadRequest.CreateDTO createDTO = VirginRoadRequest.CreateDTO.builder()
                .userId(1)
                .plans(List.of(new PlanRequest.CreateDTO()))
                .build();

        Couple couple = Couple.builder()
                .id(1)
                .female(female)
                .male(male)
                .build();

        List<Plan> plans = Arrays.asList(new Plan());
        VirginRoad virginRoad = VirginRoad.builder()
                .id(1)
                .couple(couple)
                .plans(plans).build();

        when(coupleRepository.findCoupleByUserId((createDTO.getUserId()))).thenReturn(Optional.ofNullable(couple));
        when(planRepository.saveAll(any(List.class))).thenReturn(plans);
        when(virginRoadRepository.save(any(VirginRoad.class))).thenReturn(virginRoad);

        // When
        VirginRoadResponse.SimpleResultDTO result = virginRoadService.createVirginRoad(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(coupleRepository, times(1)).findCoupleByUserId(createDTO.getUserId());
        verify(planRepository, times(1)).saveAll(any(List.class));
        verify(virginRoadRepository, times(1)).save(any(VirginRoad.class));
    }

    @Test
    public void testDeleteVirginRoad_Success() {
        // Given
        Integer userId = 1;
        User male = User.builder().id(1).build();
        User female = User.builder().id(2).build();

        Couple couple = Couple.builder()
                .id(1)
                .female(female)
                .male(male)
                .build();

        List<Plan> plans = Arrays.asList(new Plan());
        VirginRoad virginRoad = VirginRoad.builder()
                .id(1)
                .couple(couple)
                .plans(plans).build();


        when(coupleRepository.findCoupleByUserId(userId)).thenReturn(Optional.of(couple));
        when(virginRoadRepository.getVirginRoadByCouple(couple)).thenReturn(Optional.of(virginRoad));

        // When
        virginRoadService.deleteVirginRoad(userId);

        // Then
        verify(coupleRepository, times(1)).findCoupleByUserId(userId);
        verify(virginRoadRepository, times(1)).getVirginRoadByCouple(couple);
        verify(virginRoadRepository, times(1)).delete(virginRoad);
    }

}