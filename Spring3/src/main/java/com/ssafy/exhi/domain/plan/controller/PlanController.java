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

    @GetMapping("/main")
    public ResponseEntity<?> getAllPlans(
            @RequestHeader("Authorization") String accessToken
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);  // JWT에서 유저 ID 추출
        log.info("getAllPlans userId: {}", userId);

        // 유저에 해당하는 모든 플랜을 서비스에서 가져오기
        PlanResponse.AllResultDTO response = planService.getAllPlans(userId);

        // ApiResponse를 이용해서 정상적으로 응답 반환
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


    // TODO Plan의 상태를 변경하는 메서드




}
