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

//가현테스트
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleCluster;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import java.time.LocalDate;
import com.ssafy.exhi.domain.TimeWrapper;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanConverter planConverter;

    @InjectMocks
    private PlanServiceImpl planService;

    //가현테스트
    @Mock
    private CoupleRepository coupleRepository;  // CoupleRepository Mock
    @Mock
    private JWTUtil jwtUtil;  // JWTUtil Mock
    @Mock
    private TimeWrapper timeWrapper;  // TimeWrapper Mock
//    @BeforeEach
//    void setUp() {
//        // 각 테스트 메서드 실행 전에 준비해야 할 공통적인 설정이 있으면 이곳에 작성
//    }

    // plan status 성공적으로 업데이트됨
    @Test
    void testUpdatePlanStatus() {
        // Given: 현재 시간과 비교할 계획 시간 설정
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime planTimeBeforeStart = currentDateTime.plusDays(31);  // 현재 시간보다 31일 뒤
        LocalDateTime planTimeInProgress = currentDateTime.plusDays(15);   // 현재 시간보다 15일 뒤
        LocalDateTime planTimeFinished = currentDateTime.minusDays(31);    // 현재 시간보다 31일 전

        // 상태 업데이트 테스트를 위한 Plan 객체 생성
        Plan plan1 = new Plan();
        plan1.setId(1);
        plan1.setPlanTime(planTimeBeforeStart);  // BEFORE_START 상태를 기대
        plan1.updatePlanStatus(currentDateTime); // 상태 업데이트

        Plan plan2 = new Plan();
        plan2.setId(2);
        plan2.setPlanTime(planTimeInProgress);  // IN_PROGRESS 상태를 기대
        plan2.updatePlanStatus(currentDateTime); // 상태 업데이트

        Plan plan3 = new Plan();
        plan3.setId(3);
        plan3.setPlanTime(planTimeFinished);    // FINISHED 상태를 기대
        plan3.updatePlanStatus(currentDateTime); // 상태 업데이트

        // 상태 검증
        assertEquals(PlanStatus.BEFORE_START, plan1.getPlanStatus());  // BEFORE_START 상태 확인
        assertEquals(PlanStatus.IN_PROGRESS, plan2.getPlanStatus());  // IN_PROGRESS 상태 확인
        assertEquals(PlanStatus.FINISHED, plan3.getPlanStatus());  // FINISHED 상태 확인
    }


    // Plan 상태 업데이트 확인을 위해 상태를 명시적으로 설정한 후 테스트 진행
    @Test
    void testGetAllPlans() {
        // Given: 테스트를 위한 데이터 준비
        String header = "Bearer valid_jwt_token";
        Integer coupleId = 123;  // 테스트용 coupleId
        Integer userId = 1;  // 테스트용 userId

        // User 객체 생성 (Builder 패턴 사용)
        User female = User.builder()
                .id(20)
                .name("Female User")
                .loginId("female_user")
                .password("password")
                .build();

        User male = User.builder()
                .id(21)
                .name("Male User")
                .loginId("male_user")
                .password("password")
                .build();

        // VirginRoad 객체 생성
        VirginRoad virginRoad = new VirginRoad();

        // Couple 객체 생성
        Couple couple = Couple.builder()
                .id(coupleId)
                .coupleName("Couple Name")
                .female(female)
                .male(male)
                .virginRoad(virginRoad)
                .budget(10000)
                .marriageDate(LocalDate.now())
                .loveAnniversary(LocalDate.now())
                .build();

        // Plan 객체들 설정 (4개 상태 모두 추가)
        Plan plan1 = new Plan();
        plan1.setId(1);
        plan1.setPlanTime(LocalDateTime.now().plusDays(2));  // 계획 시간이 현재보다 2일 후로 설정
        plan1.updatePlanStatus(LocalDateTime.now());  // 상태 업데이트
        plan1.setPlanStatus(PlanStatus.IN_PROGRESS);

        Plan plan2 = new Plan();
        plan2.setId(2);
        plan2.setPlanTime(LocalDateTime.now().minusDays(2));  // 계획 시간이 현재보다 2일 전으로 설정
        plan2.updatePlanStatus(LocalDateTime.now());  // 상태 업데이트
        plan2.setPlanStatus(PlanStatus.NOT_TIME_CAPSULE);

        Plan plan3 = new Plan();
        plan3.setId(3);
        plan3.setPlanTime(LocalDateTime.now().plusDays(40));  // 계획 시간이 현재보다 10일 후로 설정
        plan3.updatePlanStatus(LocalDateTime.now());
        plan3.setPlanStatus(PlanStatus.BEFORE_START);

        Plan plan4 = new Plan();
        plan4.setId(4);
        plan4.setPlanTime(LocalDateTime.now().minusDays(40));  // 계획 시간이 현재보다 10일 전으로 설정
        plan4.updatePlanStatus(LocalDateTime.now());
        plan4.setPlanStatus(PlanStatus.FINISHED);

        List<Plan> plans = Arrays.asList(plan1, plan2, plan3, plan4); // plan4 추가

        // Mock 설정
        when(jwtUtil.getUserId(header)).thenReturn(userId);
        when(coupleRepository.findCoupleByUserId(userId)).thenReturn(java.util.Optional.of(couple));
        when(planRepository.findByVirginRoadCoupleId(coupleId)).thenReturn(plans);
        when(timeWrapper.now()).thenReturn(LocalDateTime.now());

        // When: 서비스 메서드 호출
        PlanResponse.AllResultDTO result = planService.getAllPlans(header);

        // Then: 결과 검증
        assertNotNull(result);
        assertNotNull(result.getGroupedPlans());

        // 상태별 검증
        assertTrue(result.getGroupedPlans().containsKey(PlanStatus.BEFORE_START)); // BEFORE_START 상태 존재 확인
        assertTrue(result.getGroupedPlans().containsKey(PlanStatus.NOT_TIME_CAPSULE)); // NOT_TIME_CAPSULE 상태 존재 확인
        assertTrue(result.getGroupedPlans().containsKey(PlanStatus.IN_PROGRESS)); // IN_PROGRESS 상태 존재 확인
        assertTrue(result.getGroupedPlans().containsKey(PlanStatus.FINISHED));  // FINISHED 상태 존재 확인

        assertEquals(1, result.getGroupedPlans().get(PlanStatus.BEFORE_START).size());  // BEFORE_START 상태의 플랜 수 확인
        assertEquals(1, result.getGroupedPlans().get(PlanStatus.NOT_TIME_CAPSULE).size()); // NOT_TIME_CAPSULE 상태의 플랜 수 확인
        assertEquals(1, result.getGroupedPlans().get(PlanStatus.IN_PROGRESS).size()); // IN_PROGRESS 상태의 플랜 수 확인
        assertEquals(1, result.getGroupedPlans().get(PlanStatus.FINISHED).size());  // FINISHED 상태의 플랜 수 확인
    }
}