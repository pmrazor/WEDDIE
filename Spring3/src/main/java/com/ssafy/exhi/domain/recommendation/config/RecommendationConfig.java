package com.ssafy.exhi.domain.recommendation.config;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.recommendation.model.dto.RoadPlanCapsuleDTO;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 추천 시스템 설정 클래스
 * - 점수 계산 로직을 한 곳에서 관리합니다.
 * - 계산된 점수를 0~100 스케일로 조정합니다.
 * - 결과값이 어떻게 측정되었는지 로그를 남깁니다.
 */
@Data
@Configuration
@Slf4j
public class RecommendationConfig {

    // ──────────────── (1) 점수 계산 파라미터 ────────────────
    private double finishedPlanBaseScore = 100.0;      // FINISHED 상태 기본 점수
    private double inProgressPlanBaseScore = 50.0;     // IN_PROGRESS 상태 기본 점수
    private double beforeStartPlanBaseScore = 0.0;     // BEFORE_START 상태 기본 점수

    private double virginRoadFinishedFactor = 1.3;     // 버진로드 완료 시 추가 가중치

    // ※ 타임캡슐 사용 여부 및 가중치
    private boolean useTimeCapsuleFactor = false;       // 타임캡슐 점수를 사용할지 여부
    private double timeCapsuleFactor = 1.2;            // 타임캡슐 가중치 (예: 1.2 → 20% 가중)

    private double recommendationThreshold = 20.0;     // 추천 임계값 (%). 이를 넘으면 추천

    // ──────────────── (2) 유사도 계산 파라미터 ────────────────
    private double cosineSimilarityWeight = 0.3;
    private double zeroVectorSimilarity = 0.8;
    private double distanceSensitivity = 0.3;

    // ──────────────── (3) 점수 계산 메서드들 ────────────────

    /**
     * RoadPlanCapsuleDTO 리스트 기반으로 플랜 점수를 계산합니다.
     * - Plan 상태별 기본 점수
     * - (옵션) 타임캡슐 점수
     * - (옵션) 버진로드 완료 가중치
     */
    public double computePlanScoreForDTOs(List<RoadPlanCapsuleDTO> planData, PlanStatus virginRoadStatus) {
        if (planData == null || planData.isEmpty()) return 0.0;

        // 1) Plan 상태별 기본 점수
        PlanStatus planStatus = planData.get(0).getPlanStatus();
        double score = getPlanStatusBaseScore(planStatus);

        // 2) 타임캡슐 점수 반영
        if (useTimeCapsuleFactor) {
            // 타임캡슐 점수 [0 ~ 1 범위]
            double capsuleScore = computeTimeCapsuleScore(planData);
            // 예시: capsuleScore가 0.5이고 timeCapsuleFactor가 1.2이면 (1 + 0.5 * 0.2) = 1 + 0.1 = 1.1배
            score *= (1.0 + (capsuleScore * (timeCapsuleFactor - 1.0)));
        }

        // 3) 버진로드가 FINISHED인 경우 추가 가중치
        if (virginRoadStatus == PlanStatus.FINISHED) {
            score *= virginRoadFinishedFactor;
        }

        log.debug("PlanStatus: {}, BaseScore: {}, VirginRoadStatus: {}, FinalScore: {}",
            planStatus, getPlanStatusBaseScore(planStatus), virginRoadStatus, score);

        return score;
    }

    /**
     * Plan 상태별 기본 점수를 반환합니다.
     */
    public double getPlanStatusBaseScore(PlanStatus status) {
        return switch (status) {
            case FINISHED -> finishedPlanBaseScore;
            case IN_PROGRESS -> inProgressPlanBaseScore;
            case BEFORE_START -> beforeStartPlanBaseScore;
            default -> 0.0;
        };
    }

    /**
     * (추가) 타임캡슐 점수를 계산합니다.
     * - planGrade가 TRUE면 +2, FALSE(null 포함)면 -1 정도로 가중치를 줘서,
     * 일정 범위 내 [0 ~ 1]로 정규화해보는 예시 로직
     */
    public double computeTimeCapsuleScore(List<RoadPlanCapsuleDTO> capsules) {
        if (capsules == null || capsules.isEmpty()) return 0.0;

        int weight = 0;
        for (RoadPlanCapsuleDTO c : capsules) {
            // timeCapsuleId가 존재한다면 타임캡슐이 있는 것으로 간주
            if (c.getTimeCapsuleId() != null) {
                Integer planGrade = c.getPlanGrade();
                if (planGrade > 3) {
                    weight += 2;   // 긍정적 평가
                } else {
                    weight -= 1;   // 부정적 평가
                }
            }
        }

        // 대략적으로 [-∞, +∞] → [-?, +?] 범위를 5로 나누어 [0~1]로 클램핑
        double rawScore = weight / 5.0;
        return Math.min(Math.max(rawScore, 0.0), 1.0);
    }

    // ──────────────── (4) 유사도 계산 로직 ────────────────

    /**
     * 코사인 유사도를 계산합니다. [0,1] 범위로 정규화됩니다.
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
        return (cos + 1.0) / 2.0;
    }

    /**
     * 거리 기반 유사도를 계산합니다. [0,1] 범위로 반환됩니다.
     */
    public double computeDistanceSimilarity(double[] v1, double[] v2) {
        double sumSq = 0.0;
        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sumSq += diff * diff;
        }
        double distance = Math.sqrt(sumSq);

        // distance가 커질수록 유사도는 감소. 다만 너무 급격히 줄어들지 않도록 distanceSensitivity를 곱
        return 0.7 + (0.3 / (1.0 + distanceSensitivity * distance));
    }

    /**
     * 최종 유사도를 계산합니다. 코사인 유사도와 거리 유사도를 가중합합니다.
     */
    public double computeFinalSimilarity(double[] v1, double[] v2) {
        double cosSim = computeCosineSimilarity(v1, v2);
        double distSim = computeDistanceSimilarity(v1, v2);
        double finalSim = (cosineSimilarityWeight * cosSim) +
            ((1 - cosineSimilarityWeight) * distSim);

        log.debug("Cosine Sim: {}, Distance Sim: {}, Final Sim: {}", cosSim, distSim, finalSim);

        return finalSim;
    }

    // ──────────────── (5) 최종 추천 여부 판단 로직 ────────────────

    /**
     * 플랜 추천 여부를 결정합니다.
     * planWeights에는 [0]=추천여부, [1]=점수합 이 들어있습니다.
     * totalPlans는 각 ServiceType별 플랜 수, totalSimilarities는 유사도 합 등을 담습니다.
     */
    public void determineRecommendations(
        Map<ServiceType, double[]> planWeights,
        Map<ServiceType, Integer> totalPlans,
        Map<ServiceType, Double> totalSimilarities
    ) {
        for (Map.Entry<ServiceType, double[]> entry : planWeights.entrySet()) {
            ServiceType serviceType = entry.getKey();
            double[] weights = entry.getValue();

            // 이미 추천(1)으로 설정되었거나, 유사 커플이 아예 없으면 건너뜀
            if (weights[0] == 1 || totalPlans.get(serviceType) == 0) {
                continue;
            }

            // (1) 현재까지 계산된 평균 플랜 점수
            double avgScore = weights[1] / totalPlans.get(serviceType);

            // (2) 동일 ServiceType의 평균 유사도
            double avgSimilarity = totalSimilarities.get(serviceType) / totalPlans.get(serviceType);

            // (3) 유사도와 점수를 혼합한 최종 점수(예시)
            //     - 가중치는 취향에 따라 조절 가능 (아래 예시: 점수 70%, 유사도 30%)
            double finalScore = (avgScore * 0.7) + (avgSimilarity * 0.3);

            // (4) 100점 만점 기준으로 % 환산
            double maxScore = finishedPlanBaseScore * virginRoadFinishedFactor;
            double percentage = (finalScore / maxScore) * 100.0;

            log.debug("ServiceType: {}, 평균 플랜점수: {}, 평균 유사도: {}, 최종점수: {}, 비율: {}",
                serviceType, avgScore, avgSimilarity, finalScore, percentage);

            // (5) recommendationThreshold(%) 이상이면 추천 상태(1)로 세팅
            if (finalScore >= recommendationThreshold) {
                weights[0] = 1;
                log.info("ServiceType {} → 추천됨! (최종점수: {})", serviceType, finalScore);
            } else {
                log.info("ServiceType {} → 추천되지 않음. (최종점수: {})", serviceType, finalScore);
            }
        }
    }
}
