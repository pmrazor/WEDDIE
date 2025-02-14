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
        log.info("getPlan userId: {}", userId);

        PlanResponse.DetailResultDTO response = planService.getPlan(userId, planId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // TODO Plan의 상태를 변경하는 메서드


    // IN_PROGRESS에서 필요한 plan과 tip 랜딩
    @GetMapping("/now")
    public ResponseEntity<?> getPlansForNow(
            @RequestHeader("Authorization") String accessToken
    ) {
        // JWT에서 userId 추출
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("getPlansForNow userId: {}", userId);

        // PlanService에서 IN_PROGRESS 상태인 Plan과 Tip을 가져옴
        PlanResponse.DetailResultDTO response = planService.getPlansForStatus(PlanStatus.IN_PROGRESS, userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

}
