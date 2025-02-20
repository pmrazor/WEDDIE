package com.ssafy.exhi.util;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Scheduler {

    private final PlanRepository planRepository;

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * * ")
    // 매일 자정마다 버진로드의 계획들의 상태를 변경
    public void dailyInit() {
        log.info("Starting daily init");
        List<Plan> plans = planRepository.findByPlanStatusIsIn(
                Arrays.asList(PlanStatus.BEFORE_START, PlanStatus.IN_PROGRESS)
        );
        for(Plan plan : plans) {
            if(plan.getStartTime() == null || plan.getEndTime() == null) return;
            plan.updatePlanStatus(LocalDateTime.now());
        }
    }
}

