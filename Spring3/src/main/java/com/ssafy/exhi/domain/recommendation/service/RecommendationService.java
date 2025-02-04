package com.ssafy.exhi.domain.recommendation.service;

import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;

/**
 * 추천 서비스 인터페이스
 * 사용자 클러스터링 및 전시회 추천 기능을 정의합니다.
 */
public interface RecommendationService {
    
    // === 클러스터링 데이터 처리 메서드 ===
    
    /**
     * 클러스터링을 위한 사용자 데이터 JSON 파일을 생성합니다.
     * (임시 구현 - 향후 Python에서 직접 DB 접근으로 대체 예정)
     * 
     * @throws com.ssafy.exhi.exception.ExceptionHandler FILE_IO_ERROR: 파일 입출력 오류 발생
     */
    void generateUserDataJSON();

    /**
     * Python 클러스터링 스크립트를 실행합니다.
     * (임시 구현 - 향후 자동 스케줄링으로 대체 예정)
     * 
     * @throws com.ssafy.exhi.exception.ExceptionHandler PYTHON_SCRIPT_EXECUTION_ERROR: Python 스크립트 실행 오류
     *                                                  PYTHON_SCRIPT_TIMEOUT: Python 스크립트 실행 시간 초과
     */
    void executeClusteringProcess();

    /**
     * 클러스터링 결과를 DB에 저장합니다.
     * (임시 구현 - 향후 Python에서 직접 DB 저장으로 대체 예정)
     * 
     * @throws com.ssafy.exhi.exception.ExceptionHandler FILE_IO_ERROR: 결과 파일 읽기 오류
     *                                                  DATABASE_ERROR: 데이터베이스 저장 오류
     */
    void processClusteringResults();

    // === 사용자 클러스터 관리 ===

    /**
     * 사용자를 클러스터에 할당합니다.
     * 
     * @param userId 사용자 ID
     * @throws com.ssafy.exhi.exception.ExceptionHandler USER_NOT_FOUND: 사용자를 찾을 수 없는 경우
     *                                                  USER_DETAIL_NOT_FOUND: 사용자 상세 정보가 없는 경우
     *                                                  CLUSTER_ASSIGNMENT_ERROR: 클러스터 할당 중 오류 발생
     */
    void addUserToCluster(Integer userId);

    // === 추천 서비스 ===

    /**
     * 사용자에게 맞춤형 플랜을 추천합니다.
     * 
     * @param userId 사용자 ID
     * @return 추천된 플랜 목록과 클러스터 정보
     * @throws com.ssafy.exhi.exception.ExceptionHandler USER_NOT_FOUND: 사용자를 찾을 수 없는 경우
     *                                                  CLUSTER_NOT_FOUND: 클러스터 정보를 찾을 수 없는 경우
     */
    RecommendationResponse.RecommendedPlansDTO getRecommendedPlans(Integer userId);
}
