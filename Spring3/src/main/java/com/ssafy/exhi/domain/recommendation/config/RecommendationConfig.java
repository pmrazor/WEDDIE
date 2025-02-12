package com.ssafy.exhi.domain.recommendation.config;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 추천 시스템 설정 클래스
 * 1. 기능별 토글 (ON/OFF)
 * 2. 점수 계산 가중치
 * 3. 임계값 및 스케일링 파라미터
 * 4. 점수 계산 로직
 */
@Data
@Configuration
public class RecommendationConfig {

    // ──────────────── [기능 토글 설정] ────────────────
    /** Plan 상태별 점수 계산 사용 여부 */
    private boolean usePlanStatusScore = true;

    /** FINISHED 상태 Plan 기본 점수 사용 여부 */
    private boolean useFinishedPlanBaseScore = true;

    /** Plan이 IN_PROGRESS일 때 기본 점수를 줄지 여부 */
    private boolean useInProgressPlanScore      = true;

    /** Plan이 BEFORE_START일 때 기본 점수를 줄지 여부 */
    private boolean useBeforeStartPlanScore     = true;

    /** 타임캡슐 점수를 사용할지 여부 */
    private boolean useTimeCapsuleWeight        = false;

    /** 버진로드가 FINISHED일 때 추가 가중치를 적용할지 여부 */
    private boolean useVirginRoadFinishedFactor = true;

    /** 커플 간 거리기반 유사도를 사용할지 여부 */
    private boolean useDistanceSimilarity       = true;

    // ──────────────── [점수 계산 파라미터] ────────────────
    /** Plan 상태별 기본 점수 */
    private double finishedPlanBaseScore = 5.0;      // FINISHED: 기본 점수 5.0 (만점 기준)
    private double inProgressPlanBaseScore = 2.5;    // IN_PROGRESS: 기본 점수 2.5
    private double beforeStartPlanBaseScore = 0.0;   // BEFORE_START: 0점

    /** 타임캡슐 가중치 (점수에 곱해짐) */
    private double timeCapsuleFactor = 1.2;          // 타임캡슐 있으면 20% 증가

    /** 버진로드 완료 가중치 (점수에 곱해짐) */
    private double virginRoadFinishedFactor = 1.3;   // 버진로드 완료시 30% 증가

    /** 추천 임계값 (%). 이 값 이상이면 추천 */
    private double recommendationThreshold = 20.0;    // 35% 이상이면 추천

    /** 최대 점수를 계산할 때 사용할 계수들 */
    private double maxPlanScoreBase = 100.0;
    private double maxTimeCapsulePoints = 20.0;
    private double maxVirginRoadFactor = 1.25;

    // ──────────────── [유사도 계산 설정] ────────────────
    /** 코사인 유사도와 거리 유사도의 가중치 비율 */
    private double cosineSimilarityWeight = 0.3;     // 코사인 유사도 30%, 거리 유사도 70%

    /** 거리 유사도 계산시 0벡터 처리를 위한 기본값 */
    private double zeroVectorSimilarity = 0.8;       // 0벡터일 때 높은 기본 유사도

    /** 거리 유사도의 민감도 (낮출수록 거리에 따른 유사도 감소가 더 완만) */
    private double distanceSensitivity = 0.3;        // 거리에 따른 감소를 매우 완만하게 조정

    // ───────────────────── [메서드들] ─────────────────────

    /**
     * Plan의 최종 점수 계산
     * 1. Plan 상태별 기본 점수
     * 2. TimeCapsule 가중치 적용 (있으면 20% 증가)
     * 3. VirginRoad 완료 가중치 적용 (완료면 50% 증가)
     */
    public double computePlanScore(Plan plan, PlanStatus virginRoadStatus) {
        double score = 0.0;

        // 1. Plan 상태별 기본 점수
        if (usePlanStatusScore) {
            score = getPlanStatusBaseScore(plan.getPlanStatus());
        }

        // 2. 타임캡슐 가중치 (있으면 20% 증가)
        if (useTimeCapsuleWeight && !plan.getTimeCapsules().isEmpty()) {
            double capsuleScore = computeTimeCapsuleScore(plan.getTimeCapsules());
            score *= (1.0 + (capsuleScore * (timeCapsuleFactor - 1.0))); 
        }

        // 3. 버진로드 완료 가중치 (완료면 50% 증가)
        if (useVirginRoadFinishedFactor && virginRoadStatus == PlanStatus.FINISHED) {
            score *= virginRoadFinishedFactor;
        }

        return score;
    }

    /**
     * Plan 상태별로 기본 점수를 적용할지(ON/OFF) + 얼마를 줄지 정의
     */
    private double getPlanStatusBaseScore(PlanStatus status) {
        // 상태별 점수를 주지만, 토글이 꺼져 있으면 0.0
        return switch (status) {

            case FINISHED -> {
                if (useFinishedPlanBaseScore) yield finishedPlanBaseScore;
                else yield 0.0;
            }

            case IN_PROGRESS -> {
                if (useInProgressPlanScore) yield inProgressPlanBaseScore;
                else yield 0.0;
            }

            case BEFORE_START -> {
                if (useBeforeStartPlanScore) yield beforeStartPlanBaseScore;
                else yield 0.0;
            }

            // 필요하다면 다른 PlanStatus( NOT_TIME_CAPSULE 등 )도 추가할 수 있음
            default -> 0.0;
        };
    }

    /**
     * TimeCapsule 점수 계산 (0.0 ~ 1.0 반환)
     * - 긍정적 평가: +2점
     * - 부정적 평가: -1점
     * - 최종 점수를 0.0~1.0 범위로 정규화
     */
    public double computeTimeCapsuleScore(List<TimeCapsule> timeCapsules) {
        if (timeCapsules.isEmpty()) return 0.0;
        int weight = timeCapsules.stream()
                .mapToInt(tc -> tc.isPlanGrade() ? -1 : 2)
                .sum();
        // 0 이상 1 이하로 정규화
        return Math.min(Math.max(weight / 5.0, 0.0), 1.0);
    }

    /**
     * 코사인 유사도 계산 [0,1]
     */
    public double computeCosineSimilarity(double[] v1, double[] v2) {
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        if (norm1 == 0.0 || norm2 == 0.0) {
            return zeroVectorSimilarity;
        }
        double cos = dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
        return (cos + 1.0) / 2.0;  // [-1,1] -> [0,1]
    }

    /**
     * 거리 기반 유사도 계산 [0,1]
     */
    public double computeDistanceSimilarity(double[] v1, double[] v2) {
        if (!useDistanceSimilarity) return 1.0;
        
        double sumSq = 0.0;
        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sumSq += diff * diff;
        }
        double distance = Math.sqrt(sumSq);
        
        // 기본 유사도를 0.7로 시작하고, 거리에 따라 천천히 감소하도록 수정
        return 0.7 + (0.3 / (1.0 + distanceSensitivity * distance));
    }

    /**
     * 최종 유사도 계산 (코사인과 거리 유사도 결합)
     */
    public double computeFinalSimilarity(double[] v1, double[] v2) {
        double cosSim = computeCosineSimilarity(v1, v2);
        double distSim = computeDistanceSimilarity(v1, v2);
        return (cosineSimilarityWeight * cosSim) + 
               ((1 - cosineSimilarityWeight) * distSim);
    }

    /**
     * 최대 점수를 계산하는 공식 (planCount * (기본점수 + 타임캡슐점수) * 버진로드가중치)
     */
    public double computeMaxPossibleScore(int planCount) {
        return planCount
                * (maxPlanScoreBase + maxTimeCapsulePoints)
                * maxVirginRoadFactor;
    }

    /**
     * 추천 여부 판단 (planScoreSum / maxScore) * 100 >= recommendationThreshold
     */
    public boolean isRecommend(double planScoreSum, double maxScore) {
        double percentage = (planScoreSum / maxScore) * 100.0;
        return (percentage >= recommendationThreshold);
    }
}