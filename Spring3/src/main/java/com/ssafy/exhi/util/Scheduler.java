package com.ssafy.exhi.util;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Scheduler {

    private final PlanRepository planRepository;

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * * ")
    // 매일 자정에 planStatus 를 옮길지 결정
    public void dailyInit() {
        log.info("Starting daily init");
        List<Plan> plans = planRepository.findByPlanStatusIsIn(Arrays.asList(PlanStatus.BEFORE_START, PlanStatus.IN_PROGRESS));
        for(Plan plan : plans) {
            if(plan.getStartTime() == null || plan.getEndTime() == null) return;
            plan.updatePlanStatus(LocalDateTime.now());
        }
    }
}

