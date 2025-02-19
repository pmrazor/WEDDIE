package com.ssafy.exhi.domain.recommendation.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.recommendation.service.ClusteringDataService;
import com.ssafy.exhi.domain.recommendation.service.RecommendationService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationControllerDocs {

    private final RecommendationService recommendationService;
    private final ClusteringDataService clusteringDataService;
    private final JWTUtil jwtUtil;

    @Value("${clustering.scheduler.enabled:false}")
    private boolean schedulerEnabled;

    @Override
    @GetMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("Adding user to clustering: userId={}", userId);
        recommendationService.addUserToCluster(userId);
        return ResponseEntity.noContent().build();
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
        clusteringDataService.executeClusteringScript();
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Override
    @GetMapping("/setJson")
    public ResponseEntity<?> setJson() {
        log.info("사용자 데이터 JSON 생성");
        clusteringDataService.generateInputDataJSON();
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Override
    @GetMapping("/getJson")
    public ResponseEntity<?> getJson() {
        log.info("클러스터링 결과 처리");
        clusteringDataService.processClusteringResults();
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Scheduled(cron = "${clustering.scheduler.cron:0 0 0 */14 * *}")
    public void scheduledClustering() {
        if (!schedulerEnabled) {
            log.info("Clustering scheduler is disabled");
            return;
        }

        log.info("Starting scheduled clustering process");
        try {
            // 1. JSON 데이터 생성
            clusteringDataService.generateInputDataJSON();
            log.info("JSON data generation completed");
            
            // 2. Python 스크립트 실행 (비동기 실행 방지)
            CompletableFuture<Boolean> pythonFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    clusteringDataService.executeClusteringScript();
                    return true;
                } catch (Exception e) {
                    log.error("Python script execution failed: ", e);
                    return false;
                }
            });

            // Python 스크립트 완료 대기 (최대 30분)
            boolean pythonSuccess = pythonFuture.get(30, TimeUnit.MINUTES);
            if (!pythonSuccess) {
                log.error("Python clustering failed");
                return;
            }
            log.info("Python script execution completed");

            // 3. 결과 처리
            clusteringDataService.processClusteringResults();
            log.info("Clustering results processing completed");
            
        } catch (TimeoutException e) {
            log.error("Clustering process timed out: ", e);
        } catch (Exception e) {
            log.error("Scheduled clustering process failed: ", e);
        }
    }
}