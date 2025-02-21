package com.ssafy.exhi.util;

import com.ssafy.exhi.domain.TimeWrapper;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
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
    private final TimeWrapper timeWrapper;

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *")
    // 매일 자정마다 버진로드의 계획들의 상태를 변경
    public void dailyInit() {
        log.info("Starting daily init");
        List<Plan> plans = planRepository.findPlansByPlanStatusNot(PlanStatus.FINISHED);
        for(Plan plan : plans) {
            plan.updatePlanStatus(timeWrapper.now());
        }
    }
}

