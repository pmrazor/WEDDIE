"""
[Wedding Recommender System - Clustering Module]

스프링 연동 프로세스:
--------------------------------
1. 초기/주기적 클러스터링 (관리자/배치)
   - 전체 사용자 데이터로 클러스터링 수행
   - JSON 형식의 모델 파라미터 파일 생성
   - 저장 위치: ${json.output.dir} (프로젝트루트/data/resources)
     ㄴ model_info.json (모델 기본 정보)
     ㄴ scalers.json (스케일러 파라미터)
     ㄴ optimized_weights.json (최적화된 특성 가중치)
     ㄴ cluster_centers.json (클러스터 중심점)
     ㄴ clustering_results.json (클러스터링 결과)
   * application-python.properties 참조

2. 신규 사용자 분류 (스프링)
   - JSON 파일들을 읽어 필요한 파라미터 로드
   - 입력 데이터 정규화 (스케일러 파라미터 사용)
   - 거리 계산으로 가장 가까운 클러스터 할당
   - 수식: min(유클리드 거리(정규화된_사용자_데이터, 각_클러스터_중심))

3. 주기적 업데이트
   - 일정 기간/데이터량 기준으로 재학습
   - 새로운 사용자 데이터 반영
   - JSON 파일들 갱신 (기존 파일 덮어쓰기)

클러스터링 프로세스:
--------------------------------
1. 초기 클러스터링 (K-means)
   - 목적: 가중치 최적화를 위한 초기 레이블 생성
   - 파라미터: n_clusters=6, n_init=20
   - 결과물: 초기 클러스터 레이블

2. 가중치 최적화 (Bayesian Optimization)
   - 입력: 초기 K-means 레이블
   - 목적: 특성별 최적 가중치 탐색
   - 탐색 범위:
       - age_weight: [2.0, 8.0] (나이)
       - budget_weight: [4.0, 12.0] (예산)
       - MBTI 4개 축 각각:
         * mbti_ie_weight: [0.5, 5.0] (내향/외향)
         * mbti_sn_weight: [0.5, 5.0] (감각/직관)
         * mbti_tf_weight: [0.5, 5.0] (사고/감정)
         * mbti_jp_weight: [0.5, 5.0] (판단/인식)
   - 결과물: 최적화된 특성 가중치

3. 최종 클러스터링
   - 최적화된 가중치 적용
   - 세 가지 방법 모두 시도:
     * K-means
     * GMM
     * Hierarchical
   - 평가 지표로 최종 모델 선택

클러스터링 방법론:
--------------------------------
1. K-Means Clustering
   - 장점: 구현이 간단하고 대규모 데이터에 효율적
   - 단점: 구형 클러스터 가정, 이상치에 민감
   - 활용: 초기 클러스터링 및 기준점 설정
   - 파라미터: n_clusters=6, n_init=20, random_state=42

2. Gaussian Mixture Model (GMM)
   - 장점: 확률적 클러스터 할당, 유연한 클러스터 형태
   - 단점: K-Means보다 계산 비용이 높음
   - 활용: 클러스터 경계가 모호한 경우 처리
   - 파라미터: n_components=6, covariance_type='full', random_state=42

3. Hierarchical Clustering
   - 장점: 클러스터 계층 구조 파악 가능
   - 단점: 대규모 데이터에 비효율적
   - 활용: 클러스터 간 관계 분석
   - 파라미터: n_clusters=6, linkage='ward'

평가 지표:
--------------------------------
1. Silhouette Score
   - 범위: [-1, 1]
   - 의미: 클러스터 응집도와 분리도 평가
   - 높을수록 좋음
   - 주요 지표로 사용

2. Davies-Bouldin Score
   - 범위: [0, ∞)
   - 의미: 클러스터 간 분리도 평가
   - 낮을수록 좋음
   - 보조 지표로 사용

3. Calinski-Harabasz Score
   - 범위: [0, ∞)
   - 의미: 클러스터 간/내 분산 비율
   - 높을수록 좋음
   - 보조 지표로 사용

4. 종합 점수 (Composite Score)
   - 계산: (silhouette * 0.5) + (1 / davies_bouldin * 0.3) + (calinski_harabasz_norm * 0.2)
   - 범위: [0, 1]
   - 가중치: 실루엣(50%), 데이비스-볼딘(30%), 칼린스키-하라바즈(20%)
   - 최종 모델 선택 기준

입력 데이터 (JSON):
--------------------------------
[
  {
    "user_id": int,      # 사용자 고유 ID (1 이상 정수)
    "age": int,          # 사용자 나이 (정수)
    "budget": int,       # 예산 (만원 단위 정수)
    "mbti": str,         # 16가지 MBTI 유형
    "routine_completed": bool  # 설문 완료 여부
  },
  ...
]

특성 처리:
--------------------------------
1. 나이 (Age)
   - 타입: 정수
   - 범위: 데이터 기반 동적 결정
   - 스케일링: StandardScaler
   - 가중치: 베이지안 최적화
   - JSON 저장: mean, scale 파라미터

2. 예산 (Budget)
   - 타입: 정수 (만원 단위)
   - 범위: 데이터 기반 동적 결정
   - 스케일링: MinMaxScaler
   - 가중치: 베이지안 최적화
   - JSON 저장: min, max 파라미터

3. MBTI
   - 처리: 4차원 이진 인코딩
   - 스케일링: 없음 (0/1)
   - 가중치: 베이지안 최적화
   - 클러스터링 시에만 UMAP 사용
"""

# ---------------------------
# (1) 필요한 라이브러리 임포트
# ---------------------------
import os
import sys
import json
import time
import traceback
from functools import wraps
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from sklearn.preprocessing import StandardScaler, MinMaxScaler
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score, davies_bouldin_score, calinski_harabasz_score
from sklearn.mixture import GaussianMixture
from sklearn.cluster import AgglomerativeClustering
from skopt import gp_minimize
from skopt.space import Real
from skopt.utils import use_named_args
import umap
import logging
from pathlib import Path
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.manifold import TSNE


# ---------------------------
# (1) 전역 설정
# ---------------------------
CONFIG = {
    # 데이터 소스 설정
    'USE_DUMMY_DATA': False,     # True: 더미데이터 생성, False: JSON 파일 사용
    'INPUT_FILE_PATH': 'input_data.json',  # JSON 입력 파일 경로
    'SAMPLE_SIZE': 10000,       # 더미데이터 생성 시 샘플 수
    
    # 클러스터링 설정
    'N_CLUSTERS': 6,            # 생성할 클러스터 수
    'RANDOM_SEED': 42,          # 랜덤 시드 (재현성 확보)
    
    # 최적화 및 시각화 설정
    'VISUALIZATION': False,      # 시각화 결과 생성 여부
    'OPTIMIZE_WEIGHTS': False,   # 특성 가중치 최적화 수행 여부
    
    # 경로 설정
    'BASE_DIR': Path(__file__).parent.absolute(),  # 현재 파이썬 파일의 디렉토리
    'RESOURCE_DIR': Path(os.getenv('RESOURCE_DIR', str(Path.cwd() / 'data' / 'resources'))),  # 환경 변수에서 경로 가져오기
    
    # JSON 파일 설정 (모두 RESOURCE_DIR 아래에 저장됨)
    'MODEL_INFO_FILE': 'model_info.json',          # 모델 기본 정보 파일
    'SCALERS_FILE': 'scalers.json',                # 스케일러 파라미터 파일
    'WEIGHTS_FILE': 'optimized_weights.json',      # 최적화된 특성 가중치 파일
    'CENTERS_FILE': 'cluster_centers.json',        # 클러스터 중심점 파일
    'RESULTS_FILE': 'clustering_results.json',     # 클러스터링 결과 파일
    
    # 캐시 사용 설정
    'USE_CACHED': {
        'MODEL_INFO': True,     # 저장된 모델 정보 사용
        'SCALERS': True,        # 저장된 스케일러 사용
        'WEIGHTS': True,        # 저장된 가중치 사용
        'CENTERS': True,        # 저장된 중심점 사용
        'RESULTS': True         # 저장된 클러스터링 결과 사용
    },
    
    # MBTI 처리 설정
    'MBTI_USE_UMAP': True,      # 클러스터링 시에만 UMAP 사용
    'MBTI_COMPONENTS': 3,
    
    # 데이터 검증 설정
    'DATA_VALIDATION': {
        'STRICT_RANGE': False,  # False: 데이터 기반 범위 사용
        'OUTLIER_DETECTION': True,  # 이상치 탐지 사용 여부
        'OUTLIER_METHOD': 'iqr',  # 'iqr' 또는 'zscore'
        'OUTLIER_THRESHOLD': 1.5  # IQR 방식의 경우 1.5
    }
}

# 클러스터별 색상 정의 (시각화용)
CLUSTER_COLORS = [
    '#E50000',  # 빨강
    '#0047AB',  # 파랑
    '#50C878',  # 초록
    '#FFD700',  # 노랑
    '#800080',  # 보라
    '#808080'   # 회색
]

# ---------------------------
# (2) 로깅 설정
# ---------------------------
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('clustering.log', encoding='utf-8')
    ]
)
logger = logging.getLogger(__name__)


# ---------------------------
# (3) 유틸리티 함수
# ---------------------------
def measure_time(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = time.time()
        result = func(*args, **kwargs)
        elapsed_time = time.time() - start_time
        logger.info(f"{func.__name__} 완료 (소요시간: {elapsed_time:.2f}초)")
        return result
    return wrapper

def resolve_path(file_path):
    path = Path(file_path)
    return path if path.is_absolute() else CONFIG['RESOURCE_DIR'] / path

@measure_time
def ensure_resource_dir():
    os.makedirs(CONFIG['RESOURCE_DIR'], exist_ok=True)

# ---------------------------
# (4) 데이터 생성 및 전처리
# ---------------------------
@measure_time
def create_dummy_data(n_samples=10000):
    """
    테스트용 더미 데이터 생성
    
    Parameters:
        n_samples (int): 생성할 샘플 수
    
    Returns:
        pd.DataFrame: 더미 데이터
    
    생성 데이터 특성:
    --------------------------------
    1. user_id: 1부터 순차적 증가
    2. age: 균일 분포 (20-45)
    3. budget: 균일 분포 (2000-10000)
    4. mbti: 실제 MBTI 분포 비율 반영
       - ISTJ (12%), ISFJ (14%), INFJ (2%)...
    5. routine_completed: True(30%), False(70%)
    """
    np.random.seed(CONFIG['RANDOM_SEED'])
    
    # 나이와 예산: 균일 분포
    ages = np.random.uniform(20, 45, n_samples)
    budgets = np.random.uniform(2000, 10000, n_samples)
    
    # MBTI: 실제 분포 반영
    mbti_types = [
        'ISTJ', 'ISFJ', 'INFJ', 'INTJ', 'ISTP', 'ISFP', 'INFP', 'INTP',
        'ESTP', 'ESFP', 'ENFP', 'ENTP', 'ESTJ', 'ESFJ', 'ENFJ', 'ENTJ'
    ]
    mbti_weights = np.array([0.12, 0.14, 0.02, 0.02, 0.05, 0.09, 0.04, 0.03,
                            0.04, 0.09, 0.08, 0.03, 0.13, 0.12, 0.02, 0.02])
    mbti_weights /= mbti_weights.sum()
    mbtis = np.random.choice(mbti_types, n_samples, p=mbti_weights)
    
    # 루틴 완료 여부: 30% 완료
    routine_completed = np.random.choice([True, False], n_samples, p=[0.3, 0.7])
    
    return pd.DataFrame({
        'user_id': range(1, n_samples + 1),
        'age': ages,
        'budget': budgets,
        'mbti': mbtis,
        'routine_completed': routine_completed
    })

@measure_time
def encode_mbti(mbti_series):
    """
    MBTI 문자열을 4차원 이진 인코딩으로 변환
    
    Parameters:
        mbti_series (pd.Series): MBTI 문자열 시리즈
    
    Returns:
        np.ndarray: 이진 인코딩된 MBTI (N x 4 행렬)
    """
    encoded = np.zeros((len(mbti_series), 4))
    encoded[:, 0] = (mbti_series.str[0] == 'E').astype(int)  # I(0)/E(1)
    encoded[:, 1] = (mbti_series.str[1] == 'N').astype(int)  # S(0)/N(1)
    encoded[:, 2] = (mbti_series.str[2] == 'F').astype(int)  # T(0)/F(1)
    encoded[:, 3] = (mbti_series.str[3] == 'P').astype(int)  # J(0)/P(1)
    return encoded

@measure_time
def encode_mbti_with_dimension_reduction(mbti_series, n_components=3, for_visualization=False):
    mbti_encoded = encode_mbti(mbti_series)
    
    reducer = umap.UMAP(
        n_components=n_components,
        n_neighbors=15,
        min_dist=0.1,
        random_state=CONFIG['RANDOM_SEED']
    )
    reduced_features = reducer.fit_transform(mbti_encoded)
    
    if for_visualization and n_components == 3:
        return reduced_features
    return reduced_features, reducer

# ---------------------------
# (5) 클러스터링 평가 함수들
# ---------------------------
@measure_time
def compute_composite_score(features, labels):
    """
    종합 점수 계산
    
    Parameters:
        features: 특성 데이터
        labels: 클러스터 레이블
    
    Returns:
        float: 종합 점수 (0~1)
    """
    silhouette = silhouette_score(features, labels)
    davies_bouldin = davies_bouldin_score(features, labels)
    calinski_harabasz = calinski_harabasz_score(features, labels)
    
    # Calinski-Harabasz 점수 정규화 (0~1)
    ch_norm = np.clip(calinski_harabasz / 10000, 0, 1)
    
    # 종합 점수 계산
    composite = (
        (silhouette + 1) * 0.5 * 0.5 +  # 실루엣 (-1~1 → 0~1)
        (1 / (davies_bouldin + 1)) * 0.3 +  # 데이비스-볼딘 (역수)
        ch_norm * 0.2  # 칼린스키-하라바즈 (정규화)
    )
    
    return float(composite)

# ---------------------------
# (6) 가중치 최적화 함수들
# ---------------------------
@measure_time
def optimize_weights(features_scaled):
    """
    베이지안 최적화를 통한 특성 가중치 최적화
    
    Parameters:
        features_scaled: 정규화된 특성 데이터
    
    Returns:
        np.array: 최적화된 가중치
    
    최적화 과정:
    --------------------------------
    1. 탐색 공간 정의
       - age_weight: [2.0, 8.0] (나이)
       - budget_weight: [4.0, 12.0] (예산)
       - MBTI 4개 축 각각:
         * mbti_ie_weight: [0.5, 5.0] (내향/외향)
         * mbti_sn_weight: [0.5, 5.0] (감각/직관)
         * mbti_tf_weight: [0.5, 5.0] (사고/감정)
         * mbti_jp_weight: [0.5, 5.0] (판단/인식)
    2. 목적 함수
       - 실루엣 점수 최대화
       - 여러 클러스터링 방법 평가
       - 최고 점수의 음수 반환
    """
    # 가중치 탐색 공간 정의
    space = [
        Real(2.0, 8.0, name='age_weight'),
        Real(5.0, 12.0, name='budget_weight'),
        Real(0.5, 5.0, name='mbti_ie_weight'),
        Real(0.5, 5.0, name='mbti_sn_weight'),
        Real(0.5, 5.0, name='mbti_tf_weight'),
        Real(0.5, 5.0, name='mbti_jp_weight')
    ]
    
    @use_named_args(space)
    def objective(**params):
        # 가중치 적용
        weights = np.array([
            params['age_weight'],
            params['budget_weight'],
            params['mbti_ie_weight'],
            params['mbti_sn_weight'],
            params['mbti_tf_weight'],
            params['mbti_jp_weight']
        ])
        weighted = features_scaled * weights
        
        scores = {}
        
        # 여러 클러스터링 방법 시도
        kmeans = KMeans(
            n_clusters=CONFIG['N_CLUSTERS'],
            n_init=20,
            random_state=CONFIG['RANDOM_SEED']
        )
        labels_kmeans = kmeans.fit_predict(weighted)
        scores['kmeans'] = silhouette_score(weighted, labels_kmeans)
        
        gmm = GaussianMixture(
            n_components=CONFIG['N_CLUSTERS'],
            random_state=CONFIG['RANDOM_SEED']
        )
        labels_gmm = gmm.fit_predict(weighted)
        scores['gmm'] = silhouette_score(weighted, labels_gmm)
        
        hierarchical = AgglomerativeClustering(
            n_clusters=CONFIG['N_CLUSTERS']
        )
        labels_hier = hierarchical.fit_predict(weighted)
        scores['hierarchical'] = silhouette_score(weighted, labels_hier)
        
        return -max(scores.values())  # 최대화를 위해 음수 반환
    
    # 베이지안 최적화 수행
    result = gp_minimize(
        objective,
        space,
        n_calls=40,
        n_random_starts=10,
        random_state=CONFIG['RANDOM_SEED']
    )
    
    return np.array(result.x)

@measure_time
def load_weights():
    """
    저장된 가중치를 JSON 파일에서 로드
    
    Returns:
        np.array: 로드된 가중치 또는 None (파일이 없는 경우)
    """
    weights_path = CONFIG['RESOURCE_DIR'] / CONFIG['WEIGHTS_FILE']
    try:
        if os.path.exists(weights_path):
            with open(weights_path, 'r') as f:
                weights_dict = json.load(f)
            
            # MBTI 가중치 배열 가져오기
            mbti_weights = weights_dict.get('mbti_weights', [1.0, 1.0, 1.0, 1.0])
            
            # 가중치 배열 구성
            weights = np.array([
                weights_dict['age_weight'],
                weights_dict['budget_weight'],
                mbti_weights[0],  # I/E
                mbti_weights[1],  # S/N
                mbti_weights[2],  # T/F
                mbti_weights[3]   # J/P
            ])
            logger.info(f"가중치 로드 완료: {weights_path}")
            return weights
    except Exception as e:
        logger.warning(f"가중치 로드 중 오류 발생: {str(e)}")
    return None

@measure_time
def load_or_compute_weights(features, initial_labels):
    """
    가중치 로드 또는 계산
    
    Parameters:
        features: 정규화된 특성 데이터
        initial_labels: 초기 클러스터링 레이블
    
    Returns:
        np.array: 최적화된 가중치
    """
    if CONFIG['OPTIMIZE_WEIGHTS']:
        return optimize_weights(features)
    else:
        loaded = load_weights()
        return loaded if loaded is not None else np.ones(features.shape[1])

# ---------------------------
# (7) 메인 클러스터링 함수
# ---------------------------
@measure_time
def perform_clustering(df, n_clusters=6):
    """
    메인 클러스터링 프로세스 수행
    
    가중치 최적화 (Bayesian Optimization):
    ------------------------------------
    1. 탐색 범위 (총 6개 파라미터)
       - age_weight: [2.0, 8.0] (나이)
       - budget_weight: [4.0, 12.0] (예산)
       - MBTI 4개 축 각각:
         * mbti_ie_weight: [0.5, 5.0] (내향/외향)
         * mbti_sn_weight: [0.5, 5.0] (감각/직관)
         * mbti_tf_weight: [0.5, 5.0] (사고/감정)
         * mbti_jp_weight: [0.5, 5.0] (판단/인식)
    
    2. 최적화 목적:
       - 클러스터링 품질 지표(composite_score) 최대화
       - 6차원 공간에서의 최적 가중치 조합 탐색
    """
    # 스케일러 객체 생성 및 저장
    standard_scaler = StandardScaler()
    minmax_scaler = MinMaxScaler()
    
    # 스케일링 수행
    age_scaled = standard_scaler.fit_transform(df[['age']])
    budget_scaled = minmax_scaler.fit_transform(df[['budget']])
    
    # MBTI 인코딩
    mbti_encoded = encode_mbti(df['mbti'])
    
    # 특성 결합 (6차원: 나이1 + 예산1 + MBTI4)
    features = np.hstack([age_scaled, budget_scaled, mbti_encoded])
    
    # 스케일러 저장
    scalers = {
        'standard_scaler': standard_scaler,
        'minmax_scaler': minmax_scaler
    }
    
    # 초기 K-Means 클러스터링
    logger.info("\n3. 초기 K-Means 클러스터링 수행...")
    initial_kmeans = KMeans(
        n_clusters=n_clusters,
        n_init=20,
        random_state=CONFIG['RANDOM_SEED']
    )
    initial_labels = initial_kmeans.fit_predict(features)
    
    # 가중치 최적화
    if CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("\n4. 베이지안 최적화로 특성 가중치 최적화 중...")
        optimized_weights = optimize_weights(features)
        logger.info(f"최적화된 가중치: {optimized_weights}")
        save_weights(optimized_weights)
    else:
        loaded = load_weights()
        optimized_weights = loaded if loaded is not None else np.ones(features.shape[1])
    
    # 가중치 적용
    features_weighted = features * optimized_weights
    
    # 최종 클러스터링 수행 (세 가지 방법)
    logger.info("\n5. 최종 클러스터링 수행 (세 가지 방법)...")
    clustering_results = {}
    
    # K-means
    kmeans = KMeans(n_clusters=n_clusters, n_init=20, random_state=CONFIG['RANDOM_SEED'])
    labels_kmeans = kmeans.fit_predict(features_weighted)
    clustering_results['kmeans'] = {
        'model': kmeans,
        'labels': labels_kmeans,
        'centers': kmeans.cluster_centers_,
        'metrics': {
            'silhouette': silhouette_score(features_weighted, labels_kmeans),
            'davies_bouldin': davies_bouldin_score(features_weighted, labels_kmeans),
            'calinski_harabasz': calinski_harabasz_score(features_weighted, labels_kmeans),
            'composite_score': compute_composite_score(features_weighted, labels_kmeans)
        },
        'cluster_sizes': np.bincount(labels_kmeans).tolist()
    }
    
    # GMM
    gmm = GaussianMixture(n_components=n_clusters, random_state=CONFIG['RANDOM_SEED'])
    labels_gmm = gmm.fit_predict(features_weighted)
    clustering_results['gmm'] = {
        'model': gmm,
        'labels': labels_gmm,
        'centers': gmm.means_,
        'metrics': {
            'silhouette': silhouette_score(features_weighted, labels_gmm),
            'davies_bouldin': davies_bouldin_score(features_weighted, labels_gmm),
            'calinski_harabasz': calinski_harabasz_score(features_weighted, labels_gmm),
            'composite_score': compute_composite_score(features_weighted, labels_gmm)
        },
        'cluster_sizes': np.bincount(labels_gmm).tolist()
    }
    
    # Hierarchical
    hierarchical = AgglomerativeClustering(n_clusters=n_clusters)
    labels_hier = hierarchical.fit_predict(features_weighted)
    clustering_results['hierarchical'] = {
        'model': hierarchical,
        'labels': labels_hier,
        'centers': None,  # 계층적 클러스터링은 중심점이 없음
        'metrics': {
            'silhouette': silhouette_score(features_weighted, labels_hier),
            'davies_bouldin': davies_bouldin_score(features_weighted, labels_hier),
            'calinski_harabasz': calinski_harabasz_score(features_weighted, labels_hier),
            'composite_score': compute_composite_score(features_weighted, labels_hier)
        },
        'cluster_sizes': np.bincount(labels_hier).tolist()
    }
    
    # 결과 저장 및 최적 모델 선택
    all_methods = ['kmeans', 'gmm', 'hierarchical']
    for method in all_methods:
        # composite_score 계산 결과 로깅
        score = clustering_results[method]['metrics']['composite_score']
        logger.info(f"{method.upper()} Composite Score: {score:.4f}")
        logger.info(f"Cluster sizes: {clustering_results[method]['cluster_sizes']}")
    
    # 최적 모델 선택
    best_method = max(all_methods, 
                     key=lambda x: clustering_results[x]['metrics']['composite_score'])
    logger.info(f"\nBest performing model: {best_method.upper()} "
               f"(score: {clustering_results[best_method]['metrics']['composite_score']:.4f})")
    
    return (
        clustering_results[best_method]['labels'],
        clustering_results[best_method].get('centers'),
        features_weighted,
        clustering_results,
        scalers
    )

@measure_time
def select_representatives(df, cluster_labels, cluster_centers, features_weighted):
    """각 클러스터의 대표 사용자 선정"""
    representatives = {}
    
    for cluster_id in range(CONFIG['N_CLUSTERS']):
        cluster_indices = np.where(cluster_labels == cluster_id)[0]
        
        if len(cluster_indices) == 0:
            logger.warning(f"Empty cluster found: {cluster_id}")
            continue
            
        # 클러스터 내 사용자들의 상태 로깅
        completed_count = df.iloc[cluster_indices]['routine_completed'].sum()
        logger.info(f"\nCluster {cluster_id}:")
        logger.info(f"Total members: {len(cluster_indices)}")
        logger.info(f"Completed routines: {completed_count}")
        
        # 중심과의 거리 계산
        if cluster_centers is not None:
            distances = np.linalg.norm(
                features_weighted[cluster_indices] - cluster_centers[cluster_id],
                axis=1
            )
            
            # routine_completed=True인 사용자 먼저 고려
            completed_mask = df.iloc[cluster_indices]['routine_completed'] == True
            if completed_mask.any():
                completed_indices = cluster_indices[completed_mask]
                completed_distances = distances[completed_mask]
                representative_idx = completed_indices[np.argmin(completed_distances)]
                logger.info("Selected representative from completed users")
            else:
                representative_idx = cluster_indices[np.argmin(distances)]
                logger.info("No completed users available, selected from all users")
        else:
            # 중심점이 없는 경우 처리
            completed_indices = cluster_indices[
                df.iloc[cluster_indices]['routine_completed']
            ]
            if len(completed_indices) > 0:
                representative_idx = np.random.choice(completed_indices)
                logger.info("Selected random completed user (no centers available)")
            else:
                representative_idx = np.random.choice(cluster_indices)
                logger.info("Selected random user (no completed users)")
        
        # 선택된 대표자 정보 로깅
        rep_user = df.iloc[representative_idx]
        representatives[cluster_id] = rep_user['user_id']
        logger.info(f"Selected representative: User {rep_user['user_id']}")
        logger.info(f"Completed: {rep_user['routine_completed']}")
        if cluster_centers is not None:
            logger.info(f"Distance to center: {distances[cluster_indices == representative_idx][0]:.4f}")
    
    return representatives

# ---------------------------
# (8) 시각화 함수들
# ---------------------------
@measure_time
def visualize_clusters_2d(df, cluster_labels, representatives):
    """2D 시각화 (나이-예산 기준, MBTI 유사도는 색상으로)"""
    # 더 큰 figsize 설정
    plt.figure(figsize=(20, 12))
    
    # 여백 확보를 위한 subplot 설정
    plt.subplot(111)
    
    # MBTI 유사도 계산 (3D와 동일한 방식)
    mbti_encoded = encode_mbti(df['mbti'])
    mbti_similarity = cosine_similarity(mbti_encoded)
    
    # 데이터 크기에 따른 perplexity 조정
    n_samples = len(df)
    perplexity = min(30, n_samples - 1)  # perplexity는 n_samples보다 작아야 함
    
    # TSNE 적용 (조정된 perplexity 사용)
    mbti_1d = TSNE(
        n_components=1,
        perplexity=perplexity,
        random_state=CONFIG['RANDOM_SEED']
    ).fit_transform(mbti_similarity)
    
    # 스케일링
    scaler = MinMaxScaler()
    mbti_1d_scaled = scaler.fit_transform(mbti_1d)
    
    # 기본 산점도 (나이 vs 예산, MBTI 유사도는 색상으로)
    scatter = plt.scatter(
        df['age'],
        df['budget'],
        c=mbti_1d_scaled,
        cmap='viridis',
        alpha=0.6,
        s=50
    )
    
    # 클러스터 중심 표시
    for cluster_id in range(max(cluster_labels) + 1):
        cluster_mask = cluster_labels == cluster_id
        center_age = df[cluster_mask]['age'].mean()
        center_budget = df[cluster_mask]['budget'].mean()
        plt.plot(
            center_age,
            center_budget,
            'x',
            color='red',
            markersize=15,
            markeredgewidth=2,
            label=f'Cluster {cluster_id} Center'
        )
    
    # 대표자 표시
    for cluster_id, rep_id in representatives.items():
        rep_data = df[df['user_id'] == rep_id].iloc[0]
        rep_idx = df[df['user_id'] == rep_id].index[0]
        plt.plot(
            rep_data['age'],
            rep_data['budget'],
            '*',
            color='black',
            markersize=15,
            markeredgewidth=2,
            label=f'Cluster {cluster_id} Rep ({rep_data["mbti"]})'
        )
    
    plt.title('Wedding Planner User Clusters\nAge vs Budget (color: MBTI Similarity)', pad=20)
    plt.xlabel('Age (years)', labelpad=10)
    plt.ylabel('Budget (10k Won)', labelpad=10)
    
    # 컬러바 추가 및 위치 조정
    cbar = plt.colorbar(scatter, pad=0.02)
    cbar.set_label('MBTI Similarity', rotation=270, labelpad=25)
    cbar.set_ticks([0, 0.5, 1])
    cbar.set_ticklabels(['Low', 'Medium', 'High'])
    
    # 범례 위치 조정 및 열 수 지정
    plt.legend(
        title="Clusters",
        loc="center left",
        bbox_to_anchor=(1.15, 0.5),
        ncol=1,  # 한 열로 표시
        borderaxespad=0,
        fontsize=10
    )
    
    plt.grid(True, linestyle='--', alpha=0.3)
    
    # 여백 자동 조정
    plt.tight_layout()
    
    # 저장 (더 높은 DPI로)
    fig_path = CONFIG['RESOURCE_DIR'] / 'clustering_visualization_2d.png'
    plt.savefig(fig_path, dpi=300, bbox_inches='tight', pad_inches=0.5)
    plt.close()
    
@measure_time
def visualize_clusters_3d(df, cluster_labels, representatives, features_weighted):
    """3D 시각화 (나이-예산-MBTI 유사도)"""
    fig = plt.figure(figsize=(15, 10))
    ax = fig.add_subplot(111, projection='3d')
    
    # MBTI 유사도 계산 (코사인 유사도 기반)
    mbti_encoded = encode_mbti(df['mbti'])
    mbti_similarity = cosine_similarity(mbti_encoded)
    
    # 데이터 크기에 따른 perplexity 조정
    n_samples = len(df)
    perplexity = min(30, n_samples - 1)  # perplexity는 n_samples보다 작아야 함
    
    # TSNE 적용 (조정된 perplexity 사용)
    mbti_1d = TSNE(
        n_components=1,
        perplexity=perplexity,
        random_state=CONFIG['RANDOM_SEED']
    ).fit_transform(mbti_similarity)
    
    # 스케일링
    scaler = MinMaxScaler()
    mbti_1d_scaled = scaler.fit_transform(mbti_1d)
    
    # 데이터 크기 확인 및 로깅
    logger.info(f"Data shapes - df: {df.shape}, cluster_labels: {len(cluster_labels)}, mbti_1d_scaled: {mbti_1d_scaled.shape}")
    
    # 산점도 그리기 (데이터 크기 확인)
    scatter = ax.scatter(
        df['age'].values,  # numpy array로 변환
        df['budget'].values,
        mbti_1d_scaled.flatten(),  # 1차원으로 평탄화
        c=cluster_labels,  # 클러스터 레이블로 색상 지정
        cmap=ListedColormap(CLUSTER_COLORS),
        alpha=0.6,
        s=50
    )
    
    # 대표자 표시
    for cluster_id, rep_id in representatives.items():
        rep_data = df[df['user_id'] == rep_id].iloc[0]
        rep_idx = df[df['user_id'] == rep_id].index[0]
        ax.scatter(
            rep_data['age'],
            rep_data['budget'],
            mbti_1d_scaled[rep_idx],
            c='black',
            marker='*',
            s=200,
            label=f'Cluster {cluster_id} Rep ({rep_data["mbti"]})'
        )
    
    ax.set_xlabel('Age (years)')
    ax.set_ylabel('Budget (10k Won)')
    ax.set_zlabel('MBTI Similarity')
    
    ax.set_zticks([0, 0.5, 1])
    ax.set_zticklabels(['Low', 'Medium', 'High'])
    
    plt.title('Wedding Planner User Clusters (3D View)\nAge vs Budget vs MBTI Similarity')
    
    legend_elements = [
        plt.Line2D([0], [0], marker='o', color='w',
                  markerfacecolor=color, label=f'Cluster {i}', markersize=10)
        for i, color in enumerate(CLUSTER_COLORS)
    ]
    ax.legend(handles=legend_elements, title="Clusters", loc="center left", bbox_to_anchor=(1.15, 0.5))
    
    plt.tight_layout()
    
    fig_path = CONFIG['RESOURCE_DIR'] / 'clustering_visualization_3d.png'
    plt.savefig(fig_path, dpi=300, bbox_inches='tight')
    plt.close()

# ---------------------------
# (9) 데이터 로드 및 전처리
# ---------------------------
@measure_time
def load_and_preprocess_data(file_path):
    """
    JSON 파일 로드 및 전처리
    
    Parameters:
        file_path: JSON 파일 경로
    
    Returns:
        pd.DataFrame: 전처리된 데이터프레임
    
    전처리 과정:
    --------------------------------
    1. 데이터 로드
       - JSON 파일 읽기
       - DataFrame 변환
       - 필수 컬럼 확인
    
    2. 데이터 검증
       - user_id: 정수형
       - age: 데이터 기반 범위
       - budget: 데이터 기반 범위
       - mbti: 16가지 유형
       - routine_completed: boolean
    
    3. 결측치 처리
       - 결측치 있는 행 제거
       - 제거된 행 수 보고
    
    4. 이상치 처리
       - age: 범위 외 값 클리핑
       - budget: 범위 외 값 클리핑
       - mbti: 유효하지 않은 값 필터링
    """
    try:
        # 절대 경로로 변환
        abs_file_path = resolve_path(file_path)
        
        if not os.path.isfile(abs_file_path):
            raise FileNotFoundError(f"Error: Input file not found at '{abs_file_path}'")
        
        # JSON 파일 로드
        with open(abs_file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # DataFrame 변환
        df = pd.DataFrame(data)
        
        # 필수 컬럼 확인
        required_columns = ['user_id', 'age', 'budget', 'mbti', 'routine_completed']
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            raise ValueError(f"Missing required columns: {missing_columns}")
        
        # 데이터 타입 변환 및 유효성 검사
        df['user_id'] = df['user_id'].astype(int)
        df['age'] = pd.to_numeric(df['age'], errors='coerce')
        df['budget'] = pd.to_numeric(df['budget'], errors='coerce')
        df['routine_completed'] = df['routine_completed'].astype(bool)
        
        # MBTI 유효성 검증
        valid_mbti = [
            'ISTJ', 'ISFJ', 'INFJ', 'INTJ', 'ISTP', 'ISFP', 'INFP', 'INTP',
            'ESTP', 'ESFP', 'ENFP', 'ENTP', 'ESTJ', 'ESFJ', 'ENFJ', 'ENTJ'
        ]
        invalid_mbti = df[~df['mbti'].isin(valid_mbti)]['mbti'].unique()
        if len(invalid_mbti) > 0:
            logger.warning(f"Warning: Found invalid MBTI types: {invalid_mbti}")
            df = df[df['mbti'].isin(valid_mbti)]
        
        # 결측치 처리
        initial_rows = len(df)
        df = df.dropna()
        dropped_rows = initial_rows - len(df)
        if dropped_rows > 0:
            logger.warning(f"Warning: Removed {dropped_rows} rows with missing values")
        
        # 이상치 처리
        if CONFIG['DATA_VALIDATION']['OUTLIER_DETECTION']:
            for col in ['age', 'budget']:
                if CONFIG['DATA_VALIDATION']['OUTLIER_METHOD'] == 'iqr':
                    Q1 = df[col].quantile(0.25)
                    Q3 = df[col].quantile(0.75)
                    IQR = Q3 - Q1
                    lower = Q1 - CONFIG['DATA_VALIDATION']['OUTLIER_THRESHOLD'] * IQR
                    upper = Q3 + CONFIG['DATA_VALIDATION']['OUTLIER_THRESHOLD'] * IQR
                    df[col] = df[col].clip(lower=lower, upper=upper)
                else:  # zscore
                    mean = df[col].mean()
                    std = df[col].std()
                    threshold = CONFIG['DATA_VALIDATION']['OUTLIER_THRESHOLD']
                    df[col] = df[col].clip(
                        lower=mean - threshold * std,
                        upper=mean + threshold * std
                    )
        
        logger.info(f"Final preprocessed data shape: {df.shape}")
        return df
        
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON format: {str(e)}")
    except Exception as e:
        raise Exception(f"Error in data preprocessing: {str(e)}")

# ---------------------------
# (10) 결과 저장 (JSON)
# ---------------------------
@measure_time
def save_clustering_results(df, cluster_labels, representatives, output_dir):
    """
    클러스터링 결과를 JSON 파일로 저장
    
    Parameters:
        df: 원본 데이터프레임
        cluster_labels: 클러스터 할당 결과
        representatives: 클러스터별 대표 user_id
        output_dir: 저장 경로
    
    저장 내용:
    --------------------------------
    1. user_clusters
       - user_id와 할당된 cluster_id 매핑
       - 형식: [{"user_id": int, "cluster_id": int}, ...]
    
    2. cluster_representatives
       - 각 클러스터의 대표 사용자 ID
       - 형식: [{"cluster_id": int, "representative_id": int}, ...]
    
    3. cluster_statistics
       - 클러스터별 통계 정보
       - 크기: 소속 사용자 수
       - 평균 나이
       - 평균 예산
       - MBTI 분포
       - 완료율: routine_completed=True 비율
       - 나이 범위: 최소/최대
       - 예산 범위: 최소/최대
    """
    # 리소스 디렉토리 생성
    ensure_resource_dir()
    
    # 결과 데이터 구조화
    df['cluster_id'] = cluster_labels
    results = {
        'user_clusters': [
            {'user_id': int(user_id), 'cluster_id': int(cluster_id)}
            for user_id, cluster_id in zip(df['user_id'], df['cluster_id'])
        ],
        'cluster_representatives': [
            {'cluster_id': int(k), 'representative_id': int(v)}
            for k, v in representatives.items()
        ],
        'cluster_statistics': {}
    }
    
    # 클러스터별 상세 통계
    for cluster_id in range(CONFIG['N_CLUSTERS']):
        cdf = df[df['cluster_id'] == cluster_id]
        results['cluster_statistics'][str(cluster_id)] = {
            'size': int(len(cdf)),
            'avg_age': float(cdf['age'].mean()),
            'avg_budget': float(cdf['budget'].mean()),
            'mbti_distribution': {k: int(v) for k, v in cdf['mbti'].value_counts().to_dict().items()},
            'completion_rate': float((cdf['routine_completed'] == True).mean()),
            'age_range': {
                'min': float(cdf['age'].min()),
                'max': float(cdf['age'].max())
            },
            'budget_range': {
                'min': float(cdf['budget'].min()),
                'max': float(cdf['budget'].max())
            }
        }
    
    # 결과 저장
    output_file = CONFIG['RESOURCE_DIR'] / CONFIG['RESULTS_FILE']
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    logger.info(f"\n클러스터링 결과 저장 완료: {output_file}")

# ---------------------------
# (11) 클러스터링 모델 저장
# ---------------------------
@measure_time
def save_model_info(model):
    """모델 기본 정보 저장"""
    model_info = {
        'model_type': type(model).__name__,
        'n_clusters': CONFIG['N_CLUSTERS'],
        'random_state': CONFIG['RANDOM_SEED'],
        'feature_ranges': {
            'age': {'min': 20, 'max': 45},
            'budget': {'min': 2000, 'max': 10000}
        },
        'mbti_encoding': {
            'method': 'binary',
            'n_components': 4
        }
    }
    
    path = CONFIG['RESOURCE_DIR'] / CONFIG['MODEL_INFO_FILE']
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(model_info, f, indent=2, ensure_ascii=False)
    logger.info(f"Saved model info to: {path}")

@measure_time
def save_scalers(scalers):
    """스케일러 파라미터 저장"""
    scalers_info = {
        'age': {
            'mean': float(scalers['standard_scaler'].mean_[0]),
            'scale': float(scalers['standard_scaler'].scale_[0])
        },
        'budget': {
            'min': float(scalers['minmax_scaler'].data_min_[0]),
            'max': float(scalers['minmax_scaler'].data_max_[0])
        }
    }
    
    path = CONFIG['RESOURCE_DIR'] / CONFIG['SCALERS_FILE']
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(scalers_info, f, indent=2, ensure_ascii=False)
    logger.info(f"Saved scalers to: {path}")

@measure_time
def save_weights(optimized_weights):
    """특성 가중치 저장"""
    weights_info = {
        'age_weight': float(optimized_weights[0]),
        'budget_weight': float(optimized_weights[1]),
        'mbti_weights': [float(w) for w in optimized_weights[2:]]
    }
    
    path = CONFIG['RESOURCE_DIR'] / CONFIG['WEIGHTS_FILE']
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(weights_info, f, indent=2, ensure_ascii=False)
    logger.info(f"Saved weights to: {path}")

@measure_time
def save_cluster_centers(cluster_centers):
    """클러스터 중심점 저장"""
    centers_info = [
        {
            'center_id': i,
            'coordinates': {
                'age': float(center[0]),
                'budget': float(center[1]),
                'mbti': [float(x) for x in center[2:]]
            }
        }
        for i, center in enumerate(cluster_centers)
    ]
    
    path = CONFIG['RESOURCE_DIR'] / CONFIG['CENTERS_FILE']
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(centers_info, f, indent=2, ensure_ascii=False)
    logger.info(f"Saved cluster centers to: {path}")

@measure_time
def save_clustering_model(model, scalers, cluster_centers, optimized_weights):
    """모든 클러스터링 정보 저장"""
    ensure_resource_dir()
    
    save_model_info(model)
    save_scalers(scalers)
    save_weights(optimized_weights)
    save_cluster_centers(cluster_centers)
    
    logger.info("All clustering information has been saved successfully!")

# ---------------------------
# (11) main() - 스크립트 진입점
# ---------------------------
def main(config=CONFIG):
    """
    메인 실행 함수
    
    Parameters:
        config: 설정 딕셔너리
    
    Returns:
        dict: 클러스터링 결과
    
    실행 순서:
    1. 데이터 로드 (더미 또는 JSON)
    2. 클러스터링 수행
    3. 대표자 선정
    4. 시각화 생성
    5. 결과 저장
    6. 모델 및 파라미터 저장
    """
    try:
        logger.info("\n=== 웨딩 추천 시스템 클러스터링 시작 ===")
        logger.info(f"Configuration: {config}")
        
        # 리소스 디렉토리 확인
        logger.info("\n1. 초기 설정")
        ensure_resource_dir()
        logger.info(f"- 더미 데이터 사용: {config['USE_DUMMY_DATA']}")
        logger.info(f"- 클러스터 수: {config['N_CLUSTERS']}")
        logger.info(f"- 리소스 경로: {config['RESOURCE_DIR']}")
        
        # 데이터 로드
        logger.info("\n2. 데이터 로드 중...")
        if config['USE_DUMMY_DATA']:
            logger.info(f"- 더미 데이터 {config['SAMPLE_SIZE']}개 생성")
            df = create_dummy_data(config['SAMPLE_SIZE'])
        else:
            input_path = resolve_path(config['INPUT_FILE_PATH'])
            logger.info(f"- 데이터 파일 로드: {input_path}")
            df = load_and_preprocess_data(input_path)
        
        logger.info(f"- 데이터 크기: {df.shape}")
        
        # 클러스터링 수행
        logger.info("\n3. 클러스터링 수행 중...")
        cluster_labels, cluster_centers, features_weighted, all_results, scalers = perform_clustering(
            df, config['N_CLUSTERS']
        )
        
        # 최적화된 가중치 로드
        logger.info("\n4. 가중치 최적화")
        optimized_weights = load_or_compute_weights(features_weighted, cluster_labels)
        logger.info("- 최적화된 가중치:")
        logger.info(f"  * 나이: {optimized_weights[0]:.2f}")
        logger.info(f"  * 예산: {optimized_weights[1]:.2f}")
        logger.info(f"  * MBTI(I/E): {optimized_weights[2]:.2f}")
        logger.info(f"  * MBTI(S/N): {optimized_weights[3]:.2f}")
        logger.info(f"  * MBTI(T/F): {optimized_weights[4]:.2f}")
        logger.info(f"  * MBTI(J/P): {optimized_weights[5]:.2f}")
        
        # 대표자 선정
        logger.info("\n5. 클러스터 대표자 선정")
        representatives = select_representatives(
            df, cluster_labels, cluster_centers, features_weighted
        )
        logger.info("- 클러스터별 대표자:")
        for cluster_id, rep_id in representatives.items():
            logger.info(f"  * 클러스터 {cluster_id}: 사용자 {rep_id}")
        
        # 시각화
        if config['VISUALIZATION']:
            logger.info("\n6. 시각화 생성 중...")
            try:
                visualize_clusters_2d(df, cluster_labels, representatives)
                visualize_clusters_3d(df, cluster_labels, representatives, features_weighted)
                logger.info("- 시각화 완료")
            except Exception as e:
                logger.error(f"- 시각화 실패: {str(e)}")
                logger.error(traceback.format_exc())
        
        # 결과 저장
        logger.info("\n7. 결과 저장 중...")
        save_clustering_results(df, cluster_labels, representatives, config['RESOURCE_DIR'])
        
        # 모델 저장
        logger.info("\n8. 모델 및 파라미터 저장")
        best_model_name = max(all_results, key=lambda k: all_results[k]['metrics']['composite_score'])
        best_model = all_results[best_model_name]['model']
        save_clustering_model(
            model=best_model,
            scalers=scalers,
            cluster_centers=cluster_centers,
            optimized_weights=optimized_weights
        )
        
        logger.info("\n=== 클러스터링 완료 ===")
        logger.info(f"- 최적 모델: {best_model_name.upper()}")
        logger.info(f"- 성능 점수: {all_results[best_model_name]['metrics']['composite_score']:.4f}")
        logger.info(f"- 결과 저장 위치: {config['RESOURCE_DIR']}")
        
        return {
            'user_clusters': df[['user_id', 'cluster_id']].to_dict('records'),
            'cluster_representatives': [
                {'cluster_id': k, 'representative_id': v}
                for k, v in representatives.items()
            ],
            'clustering_results': {
                'best_model': best_model_name,
                'metrics': all_results[best_model_name]['metrics'],
                'cluster_sizes': all_results[best_model_name]['cluster_sizes']
            }
        }
        
    except Exception as e:
        logger.error(f"\n❌ 오류 발생: {str(e)}")
        logger.error(traceback.format_exc())
        raise

# ---------------------------
# (12) 스크립트 실행
# ---------------------------
if __name__ == "__main__":
    try:
        if len(sys.argv) > 1:
            CONFIG['USE_DUMMY_DATA'] = sys.argv[1].lower() == 'true'
        if len(sys.argv) > 2:
            CONFIG['INPUT_FILE_PATH'] = sys.argv[2]
        
        main(CONFIG)
        
    except Exception as e:
        logger.error(f"Script execution failed: {str(e)}")
        logger.error(traceback.format_exc())
        sys.exit(1)