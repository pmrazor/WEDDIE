package com.ssafy.exhi.domain.recommendation.service;

/**
 * 클러스터링 데이터 처리를 위한 서비스
 */
public interface ClusteringDataService {
    
    /**
     * 클러스터링을 위한 사용자 데이터 JSON 파일을 생성합니다.
     */
    void generateInputDataJSON();

    /**
     * Python 클러스터링 스크립트를 실행합니다.
     */
    void executeClusteringScript();

    /**
     * 클러스터링 결과를 DB에 저장합니다.
     */
    void processClusteringResults();
} 