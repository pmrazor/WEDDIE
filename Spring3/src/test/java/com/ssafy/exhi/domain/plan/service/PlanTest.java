package com.ssafy.exhi.domain.plan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlanTest {

    private Plan plan;

    @BeforeEach
    void setUp() {
        plan = new Plan();
    }

    @Test
    @DisplayName("시작 시간이 null인 경우 상태가 변경되지 않아야 한다")
    void updateStatus_WithNullStartTime_ShouldNotChangeStatus() {
        // Given
        PlanStatus initialStatus = plan.getPlanStatus();
        plan.setStartTime(null);
        plan.setEndTime(LocalDateTime.now());

        // When
        plan.updatePlanStatus(LocalDateTime.now());

        // Then
        assertThat(plan.getPlanStatus()).isEqualTo(initialStatus);
    }

    @Test
    @DisplayName("종료 시간이 null인 경우 상태가 변경되지 않아야 한다")
    void updateStatus_WithNullEndTime_ShouldNotChangeStatus() {
        // Given
        PlanStatus initialStatus = plan.getPlanStatus();
        plan.setStartTime(LocalDateTime.now());
        plan.setEndTime(null);

        // When
        plan.updatePlanStatus(LocalDateTime.now());

        // Then
        assertThat(plan.getPlanStatus()).isEqualTo(initialStatus);
    }

    @Test
    @DisplayName("현재 시간이 startTime 이후이고 planTime 이전이면 IN_PROGRESS 상태가 되어야 한다")
    void updateStatus_WhenAfterStartTime_ShouldBeInProgress() {

        // Given
        LocalDateTime now = LocalDateTime.now();

        // 예약일을 현재보다 뒤로 설정
        LocalDateTime planTime = now.plusDays(10);

        // 예약일 set plan time
        plan.setPlanTime(planTime);

        // start 12 17 ~ end 02 17
        plan.calculateStartAndEndDates();

        // When
        // 지금 기준 02 18
        plan.updatePlanStatus(now);

        // Then
        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("현재 시간이 planTime+1일 이후인 경우 NOT_TIME_CAPSULE 상태가 되어야 한다")
    void updateStatus_WhenWithinOneDayOfPlanTime_ShouldBeNotTimeCapsule() {
        // Given
        // 지금 = 02 / 18
        LocalDateTime now = LocalDateTime.now();

        // 예약일 = 01 / 17
        LocalDateTime planTime = now.minusDays(32);

        // 예약일 set plan time 01 / 17
        plan.setPlanTime(planTime);

        // start 12 17 ~ end 02 17
        plan.calculateStartAndEndDates();

        // When
        // 지금 기준 02 18
        plan.updatePlanStatus(now);

        // Then
        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.NOT_TIME_CAPSULE);
    }

    @Test
    @DisplayName("현재 시간이 시작 시간 이전이고 planTime+1일 이후인 경우 상태가 변경되지 않아야 한다")
    void updateStatus_WhenAfterPlanTimePlusOneDay_ShouldNotChangeStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // planTime 35일 전으로 설정 -> 이미 끝났으니까 NOT_TIME_CAPSULE 에 멈춰있어야 함 ( FINISHED 가 되면 안 된다 )
        LocalDateTime planTime = now.minusDays(35);

        plan.setPlanTime(planTime);
        plan.calculateStartAndEndDates();

        // When
        plan.updatePlanStatus(now);

        // Then
        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.NOT_TIME_CAPSULE);
    }

    @Test
    @DisplayName("현재 시간이 시작 시간 - 30일 이전 이면 상태가 변경되지 않아야 한다")
    void updateStatus_WhenBeforeStartTimeMinusThirtyDays_ShouldNotChangeStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 한참 뒤의 일정 -> 아직 시작 전이니까 BEFORE_START 여야 함
        LocalDateTime planTime = now.plusDays(100);

        plan.setPlanTime(planTime);
        plan.calculateStartAndEndDates();
        plan.setPlanStatus(PlanStatus.BEFORE_START);

        // When
        plan.updatePlanStatus(now);

        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.BEFORE_START);
    }

    @Test
    @DisplayName("현재 시간이 시작 시간 - 30일 이전 이면 상태가 변경되지 않아야 한다")
    void aaa() {
        LocalDateTime now = LocalDateTime.now();

        // 한참 뒤의 일정 -> 아직 시작 전이니까 BEFORE_START 여야 함
        LocalDateTime planTime = now.plusDays(30);

        plan.setPlanTime(planTime);
        plan.calculateStartAndEndDates();

        // When
        plan.updatePlanStatus(now);

        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.BEFORE_START);

        // 시간 경과
        plan.updatePlanStatus(now.plusDays(10));
        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.IN_PROGRESS);

        // 시간 경과
        plan.updatePlanStatus(now.plusDays(100));
        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.NOT_TIME_CAPSULE);

    }
}