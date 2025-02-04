# Wedding Planner Clustering Module

## Overview
사용자 군집화를 통한 추천 시스템의 기반 모듈입니다.

## 주요 기능
1. 사용자 데이터 전처리
2. MBTI 차원 축소 (UMAP)
3. 클러스터링 (K-Means, GMM, Hierarchical)
4. 시각화 (2D/3D)

## 스프링 연동 가이드

### 1. 신규 사용자 클러스터 예측

```java
// 1. 파라미터 로드
JsonNode params = objectMapper.readTree(new File("cluster_params.json"));
// 2. 입력 데이터 정규화
double normalizedAge = (age - params.age.min) / (params.age.max - params.age.min);
double normalizedBudget = (budget - params.budget.min) / (params.budget.max - params.budget.min);
// 3. MBTI 인코딩 (4차원)
double[] mbtiEncoded = encodeMBTI(mbti); // [0,1,1,0] 형태
// 4. 가중치 적용
double[] weightedFeatures = applyWeights(
normalizedAge,
normalizedBudget,
mbtiEncoded,
params.feature_weights
);
// 5. 최근접 클러스터 찾기
int nearestCluster = findNearestCluster(weightedFeatures, params.cluster_centers);
```

### 2. 거리 계산 예시

```java
private double calculateDistance(double[] point1, double[] point2) {
    double sum = 0;
    for (int i = 0; i < point1.length; i++) {
        sum += Math.pow(point1[i] - point2[i], 2);
    }
    return Math.sqrt(sum);
}
```

이러한 개선사항들을 통해:
1. UMAP reducer 재사용성 향상
2. 다양한 클러스터링 평가 지표 도입
3. 시각화 일관성 개선
4. 스프링 연동 가이드 제공
