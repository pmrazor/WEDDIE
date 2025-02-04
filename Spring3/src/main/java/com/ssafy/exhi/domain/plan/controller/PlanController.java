package com.ssafy.exhi.domain.plan.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.service.PlanService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class PlanController {

    private final JWTUtil jwtUtil;
    private final PlanService planService;

    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlan(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("planId") Integer planId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("createPlan userId: {}", userId);

        PlanResponse.DetailResultDTO response = planService.getPlan(userId, planId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // TODO Plan의 상태를 변경하는 메서드 
}
