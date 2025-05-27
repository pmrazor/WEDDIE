package com.ssafy.exhi.domain.recommendation.service;

import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;

/**
 * 추천 서비스 인터페이스
 */
public interface RecommendationService {
    
    /**
     * 사용자를 클러스터에 할당합니다.
     */
    void addUserToCluster(Integer userId);

    /**
     * 사용자에게 맞춤형 플랜을 추천합니다.
     */
    RecommendationResponse.RecommendedPlansDTO getRecommendedPlans(Integer userId);
}