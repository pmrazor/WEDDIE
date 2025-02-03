package com.ssafy.exhi.domain.plan.controller;

import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.service.PlanService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.SimpleResultDTO;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class PlanController {

    private final JWTUtil jwtUtil;
    private final PlanService planService;

    /**
     * 온보딩에 사용되는 API
     * 결혼 과정에 필요한 모든 row를 생성한다.
     * @param planDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createPlan(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid PlanRequest.CreateDTO planDTO
    ) {

        Integer userId = jwtUtil.getUserId(accessToken);
        planDTO.setUserId(userId);
        log.info("createPlan userId: {}", userId);

        SimpleResultDTO response = planService.createPlan(planDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlan(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer planId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("createPlan userId: {}", userId);

        PlanResponse.DetailResultDTO response = planService.getPlan(userId, planId);
        return ResponseEntity.ok(response);
    }
}
