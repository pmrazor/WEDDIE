package com.ssafy.exhi.domain.plan.service;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.*;

import com.ssafy.exhi.domain.plan.converter.PlanConverter;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ssafy.exhi.domain.ServiceType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanConverter planConverter;

    @InjectMocks
    private PlanServiceImpl planService;

    @Test
    @DisplayName("플랜 생성 성공 테스트")
    void createPlan_Success() {
        // Given
        PlanRequest.CreateListDTO createListDTO = createSampleCreateListDTO();
        List<Plan> plans = createSamplePlans();
        List<PlanResponse.SimpleResultDTO> expectedResults = createSampleResults();

        // Mock behavior
        when(planConverter.toEntity(createListDTO)).thenReturn(plans);
        when(planRepository.saveAll(plans)).thenReturn(plans);
        when(planConverter.toDTO(plans)).thenReturn(expectedResults);

        // When
        List<PlanResponse.SimpleResultDTO> results = planService.createPlan(createListDTO);

        // Then
        assertNotNull(results);
        assertEquals(expectedResults.size(), results.size());
        verify(planConverter).toEntity(createListDTO);
        verify(planRepository).saveAll(plans);
        verify(planConverter).toDTO(plans);
    }

    @Test
    @DisplayName("빈 리스트로 플랜 생성 테스트")
    void createPlan_EmptyList() {
        // Given
        PlanRequest.CreateListDTO createListDTO = createEmptyCreateListDTO();
        List<Plan> emptyPlans = new ArrayList<>();
        List<PlanResponse.SimpleResultDTO> emptyResults = new ArrayList<>();

        // Mock behavior
        when(planConverter.toEntity(createListDTO)).thenReturn(emptyPlans);
        when(planRepository.saveAll(emptyPlans)).thenReturn(emptyPlans);
        when(planConverter.toDTO(emptyPlans)).thenReturn(emptyResults);

        // When
        List<SimpleResultDTO> results = planService.createPlan(createListDTO);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // Helper methods
    private PlanRequest.CreateListDTO createSampleCreateListDTO() {
        List<PlanRequest.CreateDTO> createDTOs = Arrays.asList(
                PlanRequest.CreateDTO.builder()
                        .shopName("Test Shop")
                        .serviceType(ServiceType.DRESS_SHOP)
                        .visible(true)
                        .planTime(LocalDateTime.now())
                        .build()
        );

        return PlanRequest.CreateListDTO.builder()
                .userId(1)
                .createDTO(createDTOs)
                .build();
    }

    private List<Plan> createSamplePlans() {
        return Arrays.asList(
                Plan.builder()
                        .id(1)
                        .shopName("Test Shop")
                        .serviceType(ServiceType.DRESS_SHOP)
                        .visible(true)
                        .planTime(LocalDateTime.now())
                        .planStatus(PlanStatus.BEFORE_START)
                        .build()
        );
    }

    private List<SimpleResultDTO> createSampleResults() {
        return Arrays.asList(
                SimpleResultDTO.builder()
                        .planId(1)
                        .planStatus(PlanStatus.BEFORE_START)
                        .serviceType(ServiceType.DRESS_SHOP)
                        .visible(true)
                        .build()
        );
    }

    private PlanRequest.CreateListDTO createEmptyCreateListDTO() {
        return PlanRequest.CreateListDTO.builder()
                .userId(1)
                .createDTO(new ArrayList<>())
                .build();
    }
}