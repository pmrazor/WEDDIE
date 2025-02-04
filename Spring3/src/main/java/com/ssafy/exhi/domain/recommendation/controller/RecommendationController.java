package com.ssafy.exhi.domain.recommendation.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.recommendation.service.RecommendationService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationControllerDocs {

    private final RecommendationService recommendationService;
    private final JWTUtil jwtUtil;

    @Override
    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("Adding user to clustering: userId={}", userId);
        recommendationService.addUserToCluster(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Override
    @GetMapping("/recommendPlans")
    public ResponseEntity<?> recommendPlans(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("Getting recommendations for user: userId={}", userId);
        RecommendationResponse.RecommendedPlansDTO recommendations = 
            recommendationService.getRecommendedPlans(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(recommendations));
    }

    @Override
    @GetMapping("/runPython")
    public ResponseEntity<?> runPython() {
        log.info("실행: Python 클러스터링 스크립트");
        recommendationService.executeClusteringProcess();
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Override
    @GetMapping("/setJson")
    public ResponseEntity<?> setJson() {
        log.info("사용자 데이터 JSON 생성");
        recommendationService.generateUserDataJSON();
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Override
    @GetMapping("/getJson")
    public ResponseEntity<?> getJson() {
        log.info("클러스터링 결과 처리");
        recommendationService.processClusteringResults();
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

}