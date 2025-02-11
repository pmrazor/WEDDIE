"""
[Wedding Recommender System - Clustering Module]

스프링 연동 프로세스:
--------------------------------
1. 초기/주기적 클러스터링 (관리자/배치)
    - 전체 커플 사용자 데이터로 클러스터링 수행
    - JSON 형식의 모델 파라미터 파일 생성
    - 저장 위치: ${json.output.dir} (프로젝트루트/data/resources)
        ㄴ model_params.json (모델 파라미터: 스케일러, 클러스터 중심점)
        ㄴ user_optimized_weights.json (사용자 클러스터링 최적화 가중치)
        ㄴ couple_optimized_weights.json (커플 클러스터링 최적화 가중치)
        ㄴ clustering_report.json (클러스터링 결과 보고서)
    * application-python.properties 참조

2. 신규 사용자 분류 (스프링)
    - JSON 파일들을 읽어 필요한 파라미터 로드(DB에 등록)
    - 입력 데이터 정규화 (스케일러 파라미터 사용)
    - 거리 계산으로 가장 가까운 클러스터 할당
    - 수식: min(유클리드 거리(정규화된_사용자_데이터, 각_클러스터_중심))

3. 주기적 업데이트
    - 일정 기간/데이터량 기준으로 재학습
    - 새로운 사용자 데이터 반영
    - JSON 파일들 갱신 (기존 파일 덮어쓰기)

4. 추후 보완 계획
    - DB 처리를 python에서 처리하여 json IO 없이 처리

클러스터링 프로세스: 
--------------------------------
1. 최적 클러스터 수 결정 (Elbow Method + 추가 평가 지표)
    - 데이터 특성에 따라 최적 클러스터 수 결정
    - 실루엣 점수 + Dunn Index(클러스터 간 분리도) 를 함께 계산하여 보조 지표로 활용
    - 최적 클러스터 수 K 결정 후 파라미터 저장
    - 시각화 결과 저장 (optional)
    
2. 가중치 최적화 (Bayesian Optimization)
    - 유저 클러스터링 가중치:
        * age_weight: [2.0, 8.0]
        * budget_weight: [4.0, 12.0]
        * mbti_ie_weight: [0.5, 6.0]
        * mbti_sn_weight: [0.5, 6.0]
        * mbti_tf_weight: [0.5, 6.0]
        * mbti_jp_weight: [0.5, 6.0]
    
    - 커플 클러스터링 가중치:
        * male_age_weight: [2.0, 6.0]
        * female_age_weight: [2.0, 6.0]
        * age_diff_weight: [0.5, 4.0]
        * budget_weight: [4.0, 10.0]
        * marriage_month_sin_weight: [0.5, 6.0]
        * marriage_month_cos_weight: [0.5, 6.0]
        * marriage_day_sin_weight: [0.5, 6.0]
        * marriage_day_cos_weight: [0.5, 6.0]
        * user_cluster_distance_weight: [0.5, 6.0]

3. 최종 클러스터링
    - 최적화된 가중치 적용
    - 세 가지 방법 모두 시도:
        * K-means
        * GMM (Gaussian Mixture Model)
        * Hierarchical (계층적 클러스터링)
    - 평가 지표 모델
        - Silhouette Score(40%)
        - Davies-Bouldin Score(20%)
        - Calinski-Harabasz Score(20%)
        - Dunn Index(20%)
        의 종합 점수로 최종 모델 선택

클러스터링 방법론:
--------------------------------
1. K-Means Clustering
   - 장점: 구현이 간단하고 대규모 데이터에 효율적
   - 단점: 구형 클러스터 가정, 이상치에 민감
   - 활용: 초기 클러스터링 및 기준점 설정
   - 파라미터: n_clusters=K, n_init=20, random_state=42

2. Gaussian Mixture Model (GMM)
   - 장점: 확률적 클러스터 할당, 유연한 클러스터 형태
   - 단점: K-Means보다 계산 비용이 높음
   - 활용: 클러스터 경계가 모호한 경우 처리
   - 파라미터: n_components=K, covariance_type='full', random_state=42

3. Hierarchical Clustering
   - 장점: 클러스터 계층 구조 파악 가능
   - 단점: 대규모 데이터에 비효율적
   - 활용: 클러스터 간 관계 분석
   - 파라미터: n_clusters=K, linkage='ward'

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

4. Dunn Index
   - 범위: [0, ∞)
   - 의미: 클러스터 간 분리도
   - 높을수록 좋음
   - 보조 지표로 사용

입력 데이터 (JSON):
--------------------------------
[
    {
        "couple_id": int,
        "female" : {
            "user_id": int,
            "age": int,
            "mbti": str,
        },
        "male" : {
            "user_id": int,
            "age": int,
            "mbti": str,
        },
        "budget": int,
        "marriage_date": str,
    },
    ...
]

특성 처리:
--------------------------------
1. 유저 클러스터링 특성
   - 나이 (age)
     * 타입: 정수
     * 스케일링: StandardScaler
     * 가중치: [2.0, 10.0]

   - 예산 (budget)
     * 타입: 정수 (만원 단위)
     * 스케일링: MinMaxScaler
     * 가중치: [4.0, 16.0]

   - MBTI
     * 처리: 4차원 이진 인코딩 [I/E, S/N, T/F, J/P]
     * 스케일링: 없음 (0/1)
     * 가중치: 각 축 [0.5, 8.0]

2. 커플 클러스터링 특성
   - 나이 관련
     * male_age, female_age (StandardScaler)
     * age_diff (MinMaxScaler)
     * 가중치: [2.0, 8.0], [0.5, 16.0]

   - 예산
     * 타입: 정수 (만원 단위)
     * 스케일링: MinMaxScaler
     * 가중치: [4.0, 16.0]

   - 결혼 예정일
     * 월/일 Cyclic encoding (sin, cos)
     * 스케일링: MinMaxScaler
     * 가중치: [0.5, 5.0], [0.1, 1.0]

   - 유저 클러스터 유사도
     * 유클리드 거리 계산
     * 스케일링: MinMaxScaler
     * 가중치: [0.5, 6.0]

특성 결합:
--------------------------------
1. 유저 클러스터링 특성 행렬
   [
    age_scaled,
    budget_scaled,
    mbti_ie,
    mbti_sn,
    mbti_tf,
    mbti_jp
   ]

2. 커플 클러스터링 특성 행렬
   [
    male_age_scaled,
    female_age_scaled,
    age_diff_scaled,
    budget_scaled,
    marriage_month_sin_scaled,
    marriage_month_cos_scaled,
    marriage_day_sin_scaled,
    marriage_day_cos_scaled,
    user_cluster_distance_scaled
   ]

평가 지표 가중치:
--------------------------------
1. 클러스터 품질 평가
   - 실루엣 점수: 40%
   - Davies-Bouldin 점수: 20%
   - Calinski-Harabasz 점수: 20%
   - Dunn Index: 20%

2. 비즈니스 평가 (추후 추가)
   - 클러스터 크기 균형: 20%
   - 예산 분포: 30%
   - 연령 분포: 30%
   - 시즌 분포: 20%

시각화 (Optional):
--------------------------------
필요시 UMAP을 통한 3D 시각화 구현
- n_components: 3
- n_neighbors: 15
- min_dist: 0.1
- metric: 'euclidean'
"""

# ---------------------------
# 1. 임포트 및 설정
# ---------------------------
import os
import sys
import json
import time
import math
from functools import wraps
import numpy as np
import pandas as pd
from pathlib import Path
import logging
from sklearn.preprocessing import StandardScaler, MinMaxScaler
from sklearn.cluster import KMeans
from sklearn.mixture import GaussianMixture
from sklearn.cluster import AgglomerativeClustering
from sklearn.metrics import (
    silhouette_score,
    davies_bouldin_score,
    calinski_harabasz_score
)
from skopt import gp_minimize
from skopt.space import Real
from skopt.utils import use_named_args
from scipy.spatial.distance import cdist
from scipy.spatial.distance import pdist, squareform

# 기존 CONFIG 정의 바로 위/아래에 삽입:
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_RESOURCE_DIR = SCRIPT_DIR.parent.parent.parent.parent / 'data' / 'resources'

CONFIG = {
    # 데이터 소스 설정
    'USE_DUMMY_DATA': False,
    # 'INPUT_FILE_PATH': 'input_data.json',  # 제거/주석
    'SAMPLE_SIZE': 2000,

    # 클러스터링 설정
    'USER_CLUSTERS': {
        'N_CLUSTERS': 9,
        'INPUT_DATA': {         # 입력 데이터 구조
            'age': 'int',       # 나이
            'budget': 'int',    # 예산 (커플 데이터에서)
            'mbti': 'str'       # MBTI
        },
        'FEATURES': [           # 전처리 후 실제 사용될 특성
            'age',              # StandardScaler
            'budget',           # MinMaxScaler
            'mbti_ie',          # 이진값 (0/1)
            'mbti_sn',          # 이진값 (0/1)
            'mbti_tf',          # 이진값 (0/1)
            'mbti_jp'           # 이진값 (0/1)
        ],
        'SCALERS': {            
            'age': StandardScaler(),
            'budget': MinMaxScaler(),
            'mbti_ie': None,    # MBTI는 이진값이므로 스케일링 불필요
            'mbti_sn': None,
            'mbti_tf': None,
            'mbti_jp': None
        },
        'WEIGHT_RANGES': {
            'age': (2.0, 10.0),
            'budget': (4.0, 16.0),
            'mbti_ie': (0.5, 8.0),
            'mbti_sn': (0.5, 8.0),
            'mbti_tf': (0.5, 8.0),
            'mbti_jp': (0.5, 8.0)
        }
    },
    
    'COUPLE_CLUSTERS': {
        'N_CLUSTERS': 9,
        'INPUT_DATA': {         
            'male_age': 'int',      
            'female_age': 'int',    
            'budget': 'int',
            'marriage_date': 'str'  
        },
        'FEATURES': [           
            'male_age',                    
            'female_age',                  
            'age_diff',                    
            'budget',
            'marriage_month_sin',     
            'marriage_month_cos',     
            'marriage_day_sin',       
            'marriage_day_cos',       
            'user_cluster_distance'        
        ],
        'SCALERS': {            
            'male_age': StandardScaler(),
            'female_age': StandardScaler(),
            'age_diff': MinMaxScaler(),
            'budget': MinMaxScaler(),
            'marriage_month_sin': MinMaxScaler(),
            'marriage_month_cos': MinMaxScaler(),
            'marriage_day_sin': MinMaxScaler(),
            'marriage_day_cos': MinMaxScaler(),
            'user_cluster_distance': MinMaxScaler()
        },
        'WEIGHT_RANGES': {
            'male_age': (2.0, 8.0),
            'female_age': (2.0, 8.0),
            'age_diff': (0.5, 12.0),
            'budget': (4.0, 16.0),
            'marriage_month_sin': (0.5, 5.0),
            'marriage_month_cos': (0.5, 5.0),
            'marriage_day_sin': (0.1, 1.0),
            'marriage_day_cos': (0.1, 1.0),
            'user_cluster_distance': (0.5, 6.0)
        }
    },
    
    'RANDOM_SEED': 42,          # 재현성을 위한 랜덤 시드
    
    # 최적화 설정
    'OPTIMIZE_WEIGHTS': False,    # 가중치 최적화 수행
    'OPTIMIZE_N_CALLS': 50,        # 최적화 호출 횟수
    'OPTIMIZATION_N_RANDOM_STARTS': 15,  # 랜덤 시작 횟수

    # 경로 설정
    # 기존: 'RESOURCE_DIR': Path(os.getenv('RESOURCE_DIR', str(Path.cwd() / 'data' / 'resources'))),
    # 수정 → 스크립트 위치 기준 fallback
    'RESOURCE_DIR': Path(os.getenv('RESOURCE_DIR', str(DEFAULT_RESOURCE_DIR))),
    
    'REPORT_FILE': 'clustering_report.json',       # 클러스터링 결과 보고서
    'MODEL_PARAMS_FILE': 'model_params.json',      # 모델 파라미터
    'WEIGHTS_FILE': 'optimized_weights.json',      # 최적화된 특성 가중치
    'USER_WEIGHTS_FILE': 'user_optimized_weights.json',
    'COUPLE_WEIGHTS_FILE': 'couple_optimized_weights.json',
}

# 클러스터별 색상 정의 (시각화용 optional)
CLUSTER_COLORS = [
    '#E50000',  # 빨강
    '#0047AB',  # 파랑
    '#50C878',  # 초록
    '#FFD700',  # 노랑
    '#800080',  # 보라
    '#808080'   # 회색
]

# 기본 가중치 설정
DEFAULT_WEIGHTS = {
    'user': {
        'weights': np.array([
            4.0,  # age_weight
            7.0,  # budget_weight
            1.0,  # mbti_ie_weight
            1.0,  # mbti_sn_weight
            1.0,  # mbti_tf_weight
            1.0   # mbti_jp_weight
        ]),
        'optimal_k': 9,  # CONFIG의 기본값
        'score': 0.0     # 초기 점수
    },
    'couple': {
        'weights': np.array([
            3.0,  # male_age_weight
            3.0,  # female_age_weight
            2.0,  # age_diff_weight
            6.0,  # couple_budget_weight
            2.0,  # marriage_month_sin_weight
            2.0,  # marriage_month_cos_weight
            2.0,  # marriage_day_sin_weight
            2.0,  # marriage_day_cos_weight
            2.0   # user_cluster_distance_weight
        ]),
        'optimal_k': 9,  # CONFIG의 기본값
        'score': 0.0     # 초기 점수
    }
}

# ---------------------------
# 3. 로깅 설정
# ---------------------------
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('clustering.log', encoding='utf-8')
    ]
)
logger = logging.getLogger(__name__)


# ---------------------------
# 4. 유틸리티 함수들
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

@measure_time
def resolve_path(file_path):
    """파일 경로 해석"""
    path = Path(file_path)
    return path if path.is_absolute() else CONFIG['RESOURCE_DIR'] / path

@measure_time
def ensure_resource_dir():
    """리소스 디렉토리 생성"""
    os.makedirs(CONFIG['RESOURCE_DIR'], exist_ok=True)

@measure_time
def load_weights(cluster_type: str) -> dict:
    """
    최적화된 가중치 정보 로드
    Args:
        cluster_type: 'user' 또는 'couple'
    Returns:
        dict: 가중치 정보 또는 기본 가중치
    """
    weights_file = CONFIG['USER_WEIGHTS_FILE'] if cluster_type == 'user' else CONFIG['COUPLE_WEIGHTS_FILE']
    
    try:
        path = resolve_path(weights_file)
        with open(path, 'r', encoding='utf-8') as f:
            weights_info = json.load(f)
            logger.info(f"{cluster_type} 가중치 파일 로드 성공: {path}")
            return {
                'weights': np.array(weights_info['weights']),
                'optimal_k': weights_info['optimal_k'],
                'score': weights_info['score']
            }
    except (FileNotFoundError, json.JSONDecodeError) as e:
        logger.warning(f"{cluster_type} 가중치 파일을 찾을 수 없거나 유효하지 않음: {str(e)}")
        logger.info(f"{cluster_type} 기본 가중치 사용")
        return DEFAULT_WEIGHTS[cluster_type]

@measure_time
def save_weights(optimized_weights: dict, cluster_type: str):
    """
    최적화된 가중치 저장
    Args:
        optimized_weights: 최적화된 가중치 정보
        cluster_type: 'user' 또는 'couple'
    """
    weights_file = CONFIG['USER_WEIGHTS_FILE'] if cluster_type == 'user' else CONFIG['COUPLE_WEIGHTS_FILE']
    
    weights_info = {
        'weights': optimized_weights['weights'].tolist(),  # numpy array를 list로 변환
        'optimal_k': optimized_weights['optimal_k'],
        'score': float(optimized_weights['score'])  # numpy float를 파이썬 float로 변환
    }
    
    try:
        path = resolve_path(weights_file)
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(weights_info, f, indent=2, ensure_ascii=False)
        logger.info(f"{cluster_type} 가중치 저장 완료: {path}")
    except Exception as e:
        logger.error(f"{cluster_type} 가중치 저장 실패: {str(e)}")
        raise


# ---------------------------
# 5. 데이터 처리 함수들
# ---------------------------

@measure_time
def validate_data_distribution(df: pd.DataFrame, cluster_type: str):
    """데이터 분포 검증"""
    logger.info(f"\n{'='*20} {cluster_type} 데이터 분포 검증 {'='*20}")
    
    if cluster_type == 'user':
        # 나이 분포
        age_stats = df['age'].describe()
        logger.info(f"나이 분포:\n{age_stats}")
        
        # 예산 분포
        budget_stats = df['budget'].describe()
        logger.info(f"예산 분포:\n{budget_stats}")
        
        # MBTI 분포
        if 'mbti' in df.columns:
            mbti_counts = df['mbti'].value_counts()
            logger.info(f"MBTI 분포:\n{mbti_counts}")
            
            # MBTI 결측치 비율
            mbti_null_ratio = df['mbti'].isna().mean()
            logger.info(f"MBTI 결측치 비율: {mbti_null_ratio:.2%}")
    
    else:  # couple
        # 남성/여성 나이 분포
        logger.info(f"남성 나이 분포:\n{df['male_age'].describe()}")
        logger.info(f"여성 나이 분포:\n{df['female_age'].describe()}")
        
        # 예산 분포
        logger.info(f"커플 예산 분포:\n{df['budget'].describe()}")
        
        # 결혼 날짜 분포
        if 'marriage_date' in df.columns:
            marriage_months = pd.to_datetime(df['marriage_date']).dt.month.value_counts()
            logger.info(f"결혼 월 분포:\n{marriage_months}")

@measure_time
def validate_feature_transformation(features: np.ndarray, feature_names: list, df: pd.DataFrame):
    """특성 변환 검증"""
    logger.info(f"\n{'='*20} 특성 변환 검증 {'='*20}")
    
    # 기본 검증
    logger.info(f"원본 데이터 크기: {df.shape}")
    logger.info(f"변환된 특성 크기: {features.shape}")
    logger.info(f"특성 이름: {feature_names}")
    
    # 특성별 통계
    for i, name in enumerate(feature_names):
        feature_stats = {
            'min': features[:, i].min(),
            'max': features[:, i].max(),
            'mean': features[:, i].mean(),
            'std': features[:, i].std(),
            'unique_values': len(np.unique(features[:, i]))
        }
        logger.info(f"\n{name} 통계:\n{feature_stats}")
        
        # 이상치 검사
        if np.isnan(features[:, i]).any():
            logger.warning(f"{name}에 NaN 값이 있습니다!")
        if np.isinf(features[:, i]).any():
            logger.warning(f"{name}에 무한대 값이 있습니다!")

@measure_time
def validate_clustering_process(model, features: np.ndarray, labels: np.ndarray, n_clusters: int):
    """클러스터링 과정 검증"""
    logger.info(f"\n{'='*20} 클러스터링 과정 검증 {'='*20}")
    
    # 기본 정보
    logger.info(f"모델 타입: {type(model).__name__}")
    logger.info(f"설정된 클러스터 수: {n_clusters}")
    
    # 레이블 검증
    unique_labels = np.unique(labels)
    logger.info(f"실제 생성된 레이블: {unique_labels}")
    
    # 클러스터 크기 분포
    cluster_sizes = np.bincount(labels)
    for i, size in enumerate(cluster_sizes):
        logger.info(f"클러스터 {i} 크기: {size} ({size/len(labels):.2%})")
    
    # 작은 클러스터 경고
    min_cluster_size = len(features) * 0.01  # 전체 데이터의 1%
    small_clusters = np.where(cluster_sizes < min_cluster_size)[0]
    if small_clusters.size > 0:
        logger.warning(f"작은 클러스터 발견: {small_clusters}")
        logger.warning(f"해당 클러스터 크기: {cluster_sizes[small_clusters]}")

@measure_time
def create_dummy_couples_json(n_couples=2000):
    """
    테스트용 더미 데이터를 JSON 형식으로 생성
    
    Returns:
        list: JSON 형식의 커플 데이터
        [
            {
                "couple_id": int,
                "female": {
                    "user_id": int,
                    "age": int,
                    "mbti": str,
                },
                "male": {
                    "user_id": int,
                    "age": int,
                    "mbti": str,
                },
                "budget": int,
                "marriage_date": str,
            },
            ...
        ]
    """
    np.random.seed(CONFIG['RANDOM_SEED'])
    
    mbti_types = [
        'ISTJ', 'ISFJ', 'INFJ', 'INTJ', 'ISTP', 'ISFP', 'INFP', 'INTP',
        'ESTP', 'ESFP', 'ENFP', 'ENTP', 'ESTJ', 'ESFJ', 'ENFJ', 'ENTJ'
    ]
    mbti_weights = np.array([0.12, 0.14, 0.02, 0.02, 0.05, 0.09, 0.04, 0.03,
                            0.04, 0.09, 0.08, 0.03, 0.13, 0.12, 0.02, 0.02])
    mbti_weights /= mbti_weights.sum()
    
    couples = []
    for i in range(n_couples):
        try:
            couple = {
                "couple_id": i + 1,
                "female": {
                    "user_id": (i * 2) + 1,
                    "age": int(np.random.randint(20, 40)),  # int로 명시적 변환
                    "mbti": np.random.choice(mbti_types, p=mbti_weights)
                },
                "male": {
                    "user_id": (i * 2) + 2,
                    "age": int(np.random.randint(20, 40)),  # int로 명시적 변환
                    "mbti": np.random.choice(mbti_types, p=mbti_weights)
                },
                "budget": int(np.random.randint(2000, 10000) * 10000),  # int로 명시적 변환
                "marriage_date": f"{np.random.randint(1, 13):02d}-{np.random.randint(1, 29):02d}"
            }
            couples.append(couple)
        except Exception as e:
            logger.error(f"더미 데이터 생성 중 오류 발생: {str(e)}")
            continue
    
    if not couples:
        logger.error("더미 데이터 생성 실패")
        raise ValueError("Failed to generate dummy data")
        
    logger.info(f"생성된 더미 커플 데이터 수: {len(couples)}")
    # 샘플 데이터 로깅
    logger.info(f"샘플 더미 데이터:\n{couples[0]}")
    
    return couples

@measure_time
def json_to_dataframe(couples_json):
    """
    JSON 형식의 커플 데이터를 사용자/커플 DataFrame으로 변환
    """
    if not couples_json:  # 빈 데이터 체크
        logger.error("입력된 JSON 데이터가 비어있습니다.")
        raise ValueError("Empty input data")

    couples_data = []
    users_data = []
    
    for couple in couples_json:
        try:
            # 필수 필드 검증
            if not all(key in couple for key in ['couple_id', 'female', 'male', 'budget', 'marriage_date']):
                logger.error(f"커플 데이터 누락: {couple}")
                continue
                
            if not all(key in couple['female'] for key in ['user_id', 'age', 'mbti']):
                logger.error(f"여성 데이터 누락: {couple['female']}")
                continue
                
            if not all(key in couple['male'] for key in ['user_id', 'age', 'mbti']):
                logger.error(f"남성 데이터 누락: {couple['male']}")
                continue

            # 커플 데이터 추가
            couple_data = {
                'couple_id': couple['couple_id'],
                'female_user_id': couple['female']['user_id'],
                'male_user_id': couple['male']['user_id'],
                'female_age': couple['female']['age'],
                'male_age': couple['male']['age'],
                'female_mbti': couple['female']['mbti'],
                'male_mbti': couple['male']['mbti'],
                'budget': couple['budget'],
                'marriage_date': couple['marriage_date']
            }
            couples_data.append(couple_data)
            
            # 여성 사용자 데이터 추가
            users_data.append({
                'user_id': couple['female']['user_id'],
                'age': couple['female']['age'],
                'mbti': couple['female']['mbti'],
                'budget': couple['budget']
            })
            
            # 남성 사용자 데이터 추가
            users_data.append({
                'user_id': couple['male']['user_id'],
                'age': couple['male']['age'],
                'mbti': couple['male']['mbti'],
                'budget': couple['budget']
            })
            
        except Exception as e:
            logger.error(f"데이터 변환 중 오류 발생: {str(e)}, 데이터: {couple}")
            continue

    if not couples_data or not users_data:
        logger.error("변환된 데이터가 비어있습니다.")
        raise ValueError("No valid data after conversion")

    # DataFrame 생성 및 데이터 타입 확인
    users_df = pd.DataFrame(users_data)
    couples_df = pd.DataFrame(couples_data)
    
    # 데이터 타입 로깅
    logger.info(f"Users DataFrame 정보:\n{users_df.info()}")
    logger.info(f"Couples DataFrame 정보:\n{couples_df.info()}")
    
    # 기본적인 데이터 검증
    logger.info(f"Users 데이터 수: {len(users_df)}")
    logger.info(f"Couples 데이터 수: {len(couples_df)}")
    
    return users_df, couples_df

@measure_time
def dataframe_to_json(df):
    """
    DataFrame을 JSON 형식으로 변환
    
    Parameters:
        df (pd.DataFrame): 변환할 데이터프레임
        
    Returns:
        list: JSON 형식의 커플 데이터
    """
    couples = []
    for _, row in df.iterrows():
        couple = {
            "couple_id": row['couple_id'],
            "female": {
                "user_id": row['female_user_id'],
                "age": row['female_age'],
                "mbti": row['female_mbti']
            },
            "male": {
                "user_id": row['male_user_id'],
                "age": row['male_age'],
                "mbti": row['male_mbti']
            },
            "budget": row['budget'],
            "marriage_date": row['marriage_date']
        }
        couples.append(couple)
    
    return couples

# ---------------------------
# 7. 특성 추출 함수들
# ---------------------------

def encode_mbti(mbti_series):
    """
    MBTI 인코딩 (미등록 유저 처리 개선)
    """
    n_samples = len(mbti_series)
    encoded = np.zeros((n_samples, 4))
    missing_count = 0
    
    for i, mbti in enumerate(mbti_series):
        if pd.isna(mbti) or not isinstance(mbti, str) or len(mbti) != 4:
            missing_count += 1
            # 중립값 대신 결측 처리를 위한 특별한 값 할당 (-1)
            encoded[i] = [-1, -1, -1, -1]
            continue
            
        mbti = mbti.upper()
        encoded[i, 0] = 1 if mbti[0] == 'E' else 0
        encoded[i, 1] = 1 if mbti[1] == 'N' else 0
        encoded[i, 2] = 1 if mbti[2] == 'F' else 0
        encoded[i, 3] = 1 if mbti[3] == 'P' else 0
    
    # MBTI 결측 비율이 높은 경우 경고
    missing_ratio = missing_count / n_samples
    if missing_ratio > 0.3:  # 30% 이상 결측
        logger.warning(f"High MBTI missing ratio: {missing_ratio:.2%}")
    
    return encoded


def encode_cyclic_features(date_str: str) -> tuple:
    """
    결혼 날짜의 순환 특성을 계산
    
    Parameters:
        date_str: 'MM-DD' 형식의 날짜 문자열
    
    Returns:
        tuple: (month_sin, month_cos, day_sin, day_cos)
    """
    try:
        if pd.isna(date_str):
            return (0.0, 1.0, 0.0, 1.0)  # 결측치의 경우 중립값
            
        # MM-DD 형식 파싱
        month, day = map(int, date_str.split('-'))
        if not (1 <= month <= 12 and 1 <= day <= 31):
            logger.warning(f"Invalid date format: {date_str}")
            return (0.0, 1.0, 0.0, 1.0)
        
        # 월 변환 (1-12 → 0-2π)
        month_angle = 2 * np.pi * (month - 1) / 12.0
        month_sin = np.sin(month_angle)
        month_cos = np.cos(month_angle)
        
        # 일 변환 (1-31 → 0-2π)
        day_angle = 2 * np.pi * (day - 1) / 31.0
        day_sin = np.sin(day_angle)
        day_cos = np.cos(day_angle)
        
        return (month_sin, month_cos, day_sin, day_cos)
        
    except Exception as e:
        logger.error(f"Error encoding cyclic features for date {date_str}: {str(e)}")
        return (0.0, 1.0, 0.0, 1.0)  # 오류 시 중립값 반환

def extract_user_features(df: pd.DataFrame):
    """
    사용자 특성 추출 및 기본 전처리 (미등록 MBTI 처리 추가)
    """
    required_columns = ['age', 'budget']  # MBTI는 선택적
    if not all(col in df.columns for col in required_columns):
        raise ValueError(f"Missing required columns. Required: {required_columns}")
    
    features = []
    feature_names = []
    
    # 수치형 특성 처리
    for feature in CONFIG['USER_CLUSTERS']['FEATURES']:
        if feature.startswith('mbti_'):
            continue
        features.append(df[feature].values.reshape(-1, 1))
        feature_names.append(feature)
    
    # MBTI 특성 처리 (있는 경우에만)
    if 'mbti' in df.columns:
        mbti_features = encode_mbti(df['mbti'])
        for i, mbti_type in enumerate(['mbti_ie', 'mbti_sn', 'mbti_tf', 'mbti_jp']):
            features.append(mbti_features[:, i].reshape(-1, 1))
            feature_names.append(mbti_type)
    else:
        # MBTI 컬럼이 없는 경우 중립값으로 채움
        for mbti_type in ['mbti_ie', 'mbti_sn', 'mbti_tf', 'mbti_jp']:
            features.append(np.full((len(df), 1), 0.5))
            feature_names.append(mbti_type)
    
    return np.hstack(features), feature_names


def extract_couple_features(df: pd.DataFrame, user_distances=None):
    """
    커플 특성 추출 및 기본 전처리
    """
    required_columns = ['male_age', 'female_age', 'budget', 'marriage_date']  # 'couple_budget' 대신 'budget'
    if not all(col in df.columns for col in required_columns):
        raise ValueError(f"Missing required columns. Required: {required_columns}")
    
    numeric_columns = ['male_age', 'female_age', 'budget']
    for col in numeric_columns:
        df[col] = pd.to_numeric(df[col], errors='coerce')
        if df[col].isna().any():
            median_val = df[col].median()
            df[col] = df[col].fillna(median_val)
            logger.warning(f"Filled {df[col].isna().sum()} missing {col} values with median: {median_val}")
    
    features = []
    feature_names = []
    
    for feature in CONFIG['COUPLE_CLUSTERS']['FEATURES']:
        if feature == 'age_diff':
            age_diff = df['male_age'] - df['female_age']
            features.append(age_diff.values.reshape(-1, 1))
        elif feature.startswith('marriage'):
            dates = df['marriage_date'].apply(encode_cyclic_features)
            if 'month_sin' in feature:
                values = [d[0] for d in dates]
            elif 'month_cos' in feature:
                values = [d[1] for d in dates]
            elif 'day_sin' in feature:
                values = [d[2] for d in dates]
            else:  # day_cos
                values = [d[3] for d in dates]
            features.append(np.array(values).reshape(-1, 1))
        else:
            features.append(df[feature].values.reshape(-1, 1))
        feature_names.append(feature)
    
    if user_distances is not None:
        features.append(user_distances.reshape(-1, 1))
        feature_names.append('user_cluster_distance')
    
    return np.hstack(features), feature_names

@measure_time
def prepare_clustering_features(df: pd.DataFrame, config: dict, cluster_type: str) -> tuple:
    """
    클러스터링을 위한 특성 준비
    
    Parameters:
        df: pd.DataFrame - 입력 데이터프레임
        config: dict - 클러스터링 설정
        cluster_type: str - 클러스터링 유형 ('user' 또는 'couple')
    
    Returns:
        tuple: (scaled_features, feature_names, scalers)
    """
    # 1. 데이터 검증
    validate_data(df, list(config['INPUT_DATA'].keys()), cluster_type)
    
    # 2. 특성 추출
    if cluster_type == 'user':
        features, feature_names = extract_user_features(df)
    else:
        features, feature_names = extract_couple_features(df)
    
    # 3. 스케일링 적용
    scaled_features = scale_features(features, feature_names, config['SCALERS'])
    
    return scaled_features, feature_names, config['SCALERS']

# ---------------------------
# 8. 스케일링 함수들
# ---------------------------

@measure_time
def scale_features(features: np.ndarray, feature_names: list, scalers: dict) -> np.ndarray:
    """
    특성 스케일링 수행
    
    Parameters:
        features: 스케일링할 특성 행렬
        feature_names: 특성 이름 리스트
        scalers: 스케일러 객체 딕셔너리
    Returns:
        np.ndarray: 스케일링된 특성 행렬
    Raises:
        ValueError: 스케일러가 정의되지 않은 특성이 있는 경우
    """
    # 모든 특성에 대한 스케일러 존재 여부 확인
    for name in feature_names:
        if name not in scalers:
            raise ValueError(f"스케일러가 정의되지 않은 특성이 있습니다: {name}")
    
    scaled_features = np.zeros_like(features)
    for i, name in enumerate(feature_names):
        if scalers[name] is None:  # MBTI와 같은 이진값
            scaled_features[:, i] = features[:, i]
        else:  # StandardScaler나 MinMaxScaler
            scaled_features[:, i] = scalers[name].fit_transform(features[:, i].reshape(-1, 1)).ravel()
    
    return scaled_features

# ---------------------------
# 9. 클러스터링 평가 함수들
# ---------------------------

def compute_dunn_index(features: np.ndarray, labels: np.array) -> float:
    """Dunn Index 계산 (빈 클러스터 처리 추가)"""
    try:
        # 실제 사용된 클러스터만 추출
        unique_labels = np.unique(labels)
        if len(unique_labels) < 2:
            logger.warning("Dunn Index 계산 불가: 유효 클러스터가 2개 미만입니다.")
            return 0.0
        
        # 클러스터별 데이터 포인트 인덱스
        cluster_points = {label: np.where(labels == label)[0] for label in unique_labels}
        
        # 빈 클러스터 제거
        cluster_points = {k: v for k, v in cluster_points.items() if len(v) > 0}
        if len(cluster_points) < 2:
            logger.warning("Dunn Index 계산 불가: 유효 클러스터가 2개 미만입니다.")
            return 0.0
            
        min_inter_cluster = float('inf')  # 클러스터 간 최소 거리
        max_intra_cluster = 0.0          # 클러스터 내 최대 거리
        
        # 클러스터 간 거리 계산
        for i in cluster_points.keys():
            points_i = features[cluster_points[i]]
            
            # 클러스터 내 거리 (지름) 계산
            if len(points_i) > 1:  # 포인트가 2개 이상일 때만
                distances = pdist(points_i)
                cluster_diameter = np.max(distances)
                max_intra_cluster = max(max_intra_cluster, cluster_diameter)
            
            # 다른 클러스터와의 거리 계산
            for j in list(cluster_points.keys())[list(cluster_points.keys()).index(i) + 1:]:
                points_j = features[cluster_points[j]]
                min_dist = np.min(cdist(points_i, points_j))
                min_inter_cluster = min(min_inter_cluster, min_dist)
        
        if max_intra_cluster == 0:
            logger.warning("Dunn Index 계산 불가: 클러스터 내 거리가 0입니다.")
            return 0.0
            
        dunn_index = min_inter_cluster / max_intra_cluster
        return float(dunn_index)
        
    except Exception as e:
        logger.warning(f"Dunn Index 계산 중 오류 발생: {str(e)}")
        return 0.0


def compute_composite_score(features, labels):
    """
    종합 점수 계산
    
    Parameters:
        features: 특성 데이터
        labels: 클러스터 레이블
    
    Returns:
        float: 종합 점수 (0~1)
        dict: 개별 점수들
    
    평가 지표 모델:
    - Silhouette Score(40%)
    - Davies-Bouldin Score(20%)
    - Calinski-Harabasz Score(20%)
    - Dunn Index(20%)
    """
    # 개별 점수 계산
    silhouette = silhouette_score(features, labels)
    davies_bouldin = davies_bouldin_score(features, labels)
    calinski_harabasz = calinski_harabasz_score(features, labels)
    dunn = compute_dunn_index(features, labels)
    
    # 점수 정규화
    silhouette_norm = (silhouette + 1) * 0.5  # -1~1 → 0~1
    davies_bouldin_norm = 1 / (davies_bouldin + 1)  # 낮을수록 좋음 → 0~1
    ch_norm = np.clip(calinski_harabasz / 10000, 0, 1)  # 임의의 상한값으로 정규화
    dunn_norm = np.clip(dunn, 0, 1)  # 0~1로 클리핑
    
    # 가중치 적용하여 종합 점수 계산
    composite = (
        silhouette_norm * 0.4 +      # Silhouette Score(40%)
        davies_bouldin_norm * 0.2 +  # Davies-Bouldin Score(20%)
        ch_norm * 0.2 +              # Calinski-Harabasz Score(20%)
        dunn_norm * 0.2              # Dunn Index(20%)
    )
    
    # 개별 점수들도 함께 반환
    scores = {
        'composite': float(composite),
        'silhouette': float(silhouette),
        'davies_bouldin': float(davies_bouldin),
        'calinski_harabasz': float(calinski_harabasz),
        'dunn_index': float(dunn)
    }
    
    return scores

@measure_time
def evaluate_models(features: np.ndarray, n_clusters: int) -> dict:
    """여러 클러스터링 모델 평가 (검증 추가)"""
    if features.size == 0:
        raise ValueError("빈 특성 행렬이 입력되었습니다.")
    
    # 입력 데이터 검증
    validate_feature_transformation(features, ['feature_'+str(i) for i in range(features.shape[1])], pd.DataFrame())
    
    logger.info(f"특성 행렬 크기: {features.shape}")
    logger.info(f"클러스터 수: {n_clusters}")
    
    results = {}
    max_retries = 3
    
    for model_type in ['kmeans', 'gmm', 'hierarchical']:
        for attempt in range(max_retries):
            try:
                model = create_clustering_model(model_type, n_clusters)
                labels = model.fit_predict(features)
                
                # 클러스터링 과정 검증
                validate_clustering_process(model, features, labels, n_clusters)
                
                # 클러스터 크기 분포 확인
                unique_labels, counts = np.unique(labels, return_counts=True)
                cluster_sizes = dict(zip(unique_labels, counts))
                
                # 작은 클러스터 처리
                if min(counts) < features.shape[0] * 0.01:
                    if attempt < max_retries - 1:
                        logger.warning(f"{model_type}: 너무 작은 클러스터 발견. 재시도 {attempt + 1}/{max_retries}")
                        continue
                    else:
                        logger.warning(f"{model_type}: 최대 재시도 횟수 도달. 현재 결과 사용")
                
                scores = compute_composite_score(features, labels)
                logger.info(f"{model_type} 클러스터링 점수:")
                for metric, value in scores.items():
                    logger.info(f"- {metric}: {value:.4f}")
                
                results[model_type] = {
                    'model': model,
                    'labels': labels,
                    'scores': scores,
                    'cluster_sizes': cluster_sizes
                }
                break
                
            except Exception as e:
                if attempt < max_retries - 1:
                    logger.warning(f"{model_type} 클러스터링 실패, 재시도 {attempt + 1}/{max_retries}: {str(e)}")
                    continue
                else:
                    logger.error(f"{model_type} 클러스터링 최종 실패: {str(e)}")
                    raise
    
    if not results:
        raise ValueError("모든 클러스터링 모델이 실패했습니다.")
    
    return results



# ---------------------------
# 10. 가중치 최적화 함수들
# ---------------------------

@measure_time
def optimize_user_weights(features_scaled):
    """유저 클러스터링을 위한 가중치 최적화"""
    # 이전에 최적화된 가중치 로드
    loaded_weights = load_weights('user')
    if loaded_weights and not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("이전에 최적화된 가중치 사용")
        # 가중치 저장 추가
        save_weights(loaded_weights, 'user')
        return loaded_weights
    
    # 가중치 파일이 없으면 DEFAULT_WEIGHTS 사용
    if not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("기본 가중치 사용")
        # 기본 가중치도 저장
        save_weights(DEFAULT_WEIGHTS['user'], 'user')
        return DEFAULT_WEIGHTS['user']
    
    # 최적 K 결정
    optimal_k = find_optimal_k(features_scaled)
    logger.info(f"Elbow Method로 찾은 최적 K: {optimal_k}")
    
    # K가 CONFIG의 기본값보다 작으면 기본값 사용
    if optimal_k < CONFIG['USER_CLUSTERS']['N_CLUSTERS']:
        logger.warning(f"찾은 K({optimal_k})가 최소값({CONFIG['USER_CLUSTERS']['N_CLUSTERS']}) 미만이어서 기본값 사용")
        optimal_k = CONFIG['USER_CLUSTERS']['N_CLUSTERS']
    
    # CONFIG에서 가중치 범위 가져오기
    weight_ranges = CONFIG['USER_CLUSTERS']['WEIGHT_RANGES']
    space = [
        Real(weight_ranges['age'][0], weight_ranges['age'][1], name='age_weight'),
        Real(weight_ranges['budget'][0], weight_ranges['budget'][1], name='budget_weight'),
        Real(weight_ranges['mbti_ie'][0], weight_ranges['mbti_ie'][1], name='mbti_ie_weight'),
        Real(weight_ranges['mbti_sn'][0], weight_ranges['mbti_sn'][1], name='mbti_sn_weight'),
        Real(weight_ranges['mbti_tf'][0], weight_ranges['mbti_tf'][1], name='mbti_tf_weight'),
        Real(weight_ranges['mbti_jp'][0], weight_ranges['mbti_jp'][1], name='mbti_jp_weight')
    ]
    
    @use_named_args(space)
    def objective(**params):
        weights = np.array([
            params['age_weight'],
            params['budget_weight'],
            params['mbti_ie_weight'],
            params['mbti_sn_weight'],
            params['mbti_tf_weight'],
            params['mbti_jp_weight']
        ])
        weighted_features = features_scaled * weights
        
        # 세 가지 클러스터링 방법 시도
        scores = {}
        
        # K-means
        kmeans = KMeans(n_clusters=optimal_k, n_init=20, random_state=CONFIG['RANDOM_SEED'])
        labels_kmeans = kmeans.fit_predict(weighted_features)
        scores['kmeans'] = compute_composite_score(weighted_features, labels_kmeans)
        
        # GMM
        gmm = GaussianMixture(n_components=optimal_k, random_state=CONFIG['RANDOM_SEED'])
        labels_gmm = gmm.fit_predict(weighted_features)
        scores['gmm'] = compute_composite_score(weighted_features, labels_gmm)
        
        # Hierarchical
        hierarchical = AgglomerativeClustering(n_clusters=optimal_k)
        labels_hier = hierarchical.fit_predict(weighted_features)
        scores['hier'] = compute_composite_score(weighted_features, labels_hier)
        
        # 최고 점수 반환
        best_score = max(scores.values(), key=lambda x: x['composite'])
        return -best_score['composite']
    
    # 베이지안 최적화 수행
    result = gp_minimize(
        objective,
        space,
        n_calls=CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts=CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state=CONFIG['RANDOM_SEED']
    )
    
    optimized_weights = {
        'weights': np.array(result.x),
        'optimal_k': optimal_k,
        'score': -result.fun
    }
    
    # 최적화된 가중치 저장
    save_weights(optimized_weights, 'user')
    
    return optimized_weights

@measure_time
def optimize_couple_weights(features_scaled):
    """커플 클러스터링을 위한 가중치 최적화"""
    # 이전에 최적화된 가중치 로드
    loaded_weights = load_weights('couple')
    if loaded_weights and not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("이전에 최적화된 가중치 사용")
        # 가중치 저장 추가
        save_weights(loaded_weights, 'couple')
        return loaded_weights
    
    # 가중치 파일이 없으면 DEFAULT_WEIGHTS 사용
    if not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("기본 가중치 사용")
        # 기본 가중치도 저장
        save_weights(DEFAULT_WEIGHTS['couple'], 'couple')
        return DEFAULT_WEIGHTS['couple']
    
    # 최적 K 결정
    optimal_k = find_optimal_k(features_scaled)
    logger.info(f"Elbow Method로 찾은 최적 K: {optimal_k}")
    
    # K가 CONFIG의 기본값보다 작으면 기본값 사용
    if optimal_k < CONFIG['COUPLE_CLUSTERS']['N_CLUSTERS']:
        logger.warning(f"찾은 K({optimal_k})가 최소값({CONFIG['COUPLE_CLUSTERS']['N_CLUSTERS']}) 미만이어서 기본값 사용")
        optimal_k = CONFIG['COUPLE_CLUSTERS']['N_CLUSTERS']
    
    # CONFIG에서 가중치 범위 가져오기
    weight_ranges = CONFIG['COUPLE_CLUSTERS']['WEIGHT_RANGES']
    space = [
        Real(weight_ranges['male_age'][0], weight_ranges['male_age'][1], name='male_age_weight'),
        Real(weight_ranges['female_age'][0], weight_ranges['female_age'][1], name='female_age_weight'),
        Real(weight_ranges['age_diff'][0], weight_ranges['age_diff'][1], name='age_diff_weight'),
        Real(weight_ranges['budget'][0], weight_ranges['budget'][1], name='budget_weight'),
        Real(weight_ranges['marriage_month_sin'][0], weight_ranges['marriage_month_sin'][1], name='marriage_month_sin_weight'),
        Real(weight_ranges['marriage_month_cos'][0], weight_ranges['marriage_month_cos'][1], name='marriage_month_cos_weight'),
        Real(weight_ranges['marriage_day_sin'][0], weight_ranges['marriage_day_sin'][1], name='marriage_day_sin_weight'),
        Real(weight_ranges['marriage_day_cos'][0], weight_ranges['marriage_day_cos'][1], name='marriage_day_cos_weight'),
        Real(weight_ranges['user_cluster_distance'][0], weight_ranges['user_cluster_distance'][1], name='user_cluster_distance_weight')
    ]
    
    @use_named_args(space)
    def objective(**params):
        weights = np.array([
            params['male_age_weight'],
            params['female_age_weight'],
            params['age_diff_weight'],
            params['budget_weight'],
            params['marriage_month_sin_weight'],
            params['marriage_month_cos_weight'],
            params['marriage_day_sin_weight'],
            params['marriage_day_cos_weight'],
            params['user_cluster_distance_weight']
        ])
        weighted_features = features_scaled * weights
        
        # 세 가지 클러스터링 방법 시도
        scores = {}
        
        # K-means
        kmeans = KMeans(n_clusters=optimal_k, n_init=20, random_state=CONFIG['RANDOM_SEED'])
        labels_kmeans = kmeans.fit_predict(weighted_features)
        scores['kmeans'] = compute_composite_score(weighted_features, labels_kmeans)
        
        # GMM
        gmm = GaussianMixture(n_components=optimal_k, random_state=CONFIG['RANDOM_SEED'])
        labels_gmm = gmm.fit_predict(weighted_features)
        scores['gmm'] = compute_composite_score(weighted_features, labels_gmm)
        
        # Hierarchical
        hierarchical = AgglomerativeClustering(n_clusters=optimal_k)
        labels_hier = hierarchical.fit_predict(weighted_features)
        scores['hier'] = compute_composite_score(weighted_features, labels_hier)
        
        # 최고 점수 반환
        best_score = max(scores.values(), key=lambda x: x['composite'])
        return -best_score['composite']
    
    # 베이지안 최적화 수행
    result = gp_minimize(
        objective,
        space,
        n_calls=CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts=CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state=CONFIG['RANDOM_SEED']
    )
    
    optimized_weights = {
        'weights': np.array(result.x),
        'optimal_k': optimal_k,
        'score': -result.fun
    }
    
    # 최적화된 가중치 저장
    save_weights(optimized_weights, 'couple')
    
    return optimized_weights

@measure_time
def preprocess_data(cluster_type: str = 'user') -> tuple:
    """
    데이터 전처리 (user/couple 공용)
    """
    # 설정 가져오기
    if cluster_type == 'user':
        config = CONFIG['USER_CLUSTERS']
    else:
        config = CONFIG['COUPLE_CLUSTERS']
    
    # 1. 데이터 로드
    if CONFIG['USE_DUMMY_DATA']:
        logger.info(f"- 더미 데이터 {CONFIG['SAMPLE_SIZE']}개 생성")
        couples_json = create_dummy_couples_json(CONFIG['SAMPLE_SIZE'])
        users_df, couples_df = json_to_dataframe(couples_json)
    else:
        data_file = CONFIG['RESOURCE_DIR'] / 'input_data.json'
        with open(data_file, 'r', encoding='utf-8') as f:
            couples_json = json.load(f)
            users_df, couples_df = json_to_dataframe(couples_json)
    
    # 2. user/couple 분기
    if cluster_type == 'user':
        df = users_df
    else:
        df = couples_df
    
    # 3. 기본 데이터 검증
    validate_data(df, config['INPUT_DATA'].keys(), cluster_type)
    
    # 4. 숫자형 데이터 전처리(결측치/이상치 처리 등)
    numeric_columns = [col for col, dtype in config['INPUT_DATA'].items() 
                       if dtype in ('int', 'float')]
    df = clean_numeric_data(df, numeric_columns)
    
    # 5-a. user인 경우 → 여기서 바로 피처 준비
    if cluster_type == 'user':
        features, feature_names, scalers = prepare_clustering_features(df, config, cluster_type)
        return features, scalers, df
    else:
        # 5-b. couple인 경우 → user_cluster_distance 등은 이후 evaluate_couple_clustering()에서 처리
        # 여기서는 df만 반환하고, 별도로 반환할 features는 없음
        return None, None, df

# ---------------------------
# 11. 클러스터링 모델 생성/평가 함수들
# ---------------------------

@measure_time
def create_clustering_model(model_type: str, n_clusters: int):
    """클러스터링 모델 생성"""
    if model_type == 'kmeans':
        return KMeans(
            n_clusters=n_clusters,
            init='k-means++',  # 초기화 방식 명시
            n_init=50,         # 초기화 시도 횟수 증가
            max_iter=500,      # 최대 반복 횟수 증가
            random_state=CONFIG['RANDOM_SEED']
        )
    elif model_type == 'gmm':
        return GaussianMixture(
            n_components=n_clusters,
            covariance_type='full',
            n_init=50,         # 초기화 시도 횟수 증가
            random_state=CONFIG['RANDOM_SEED']
        )
    elif model_type == 'hierarchical':
        return AgglomerativeClustering(
            n_clusters=n_clusters,
            linkage='ward'
        )
    else:
        raise ValueError(f"지원하지 않는 모델 타입: {model_type}")
    
@measure_time
def validate_clustering_results(labels, n_clusters, model_name):
    """클러스터링 결과 검증"""
    unique_labels = np.unique(labels)
    cluster_sizes = np.bincount(labels)
    
    # 기본 검증
    if len(labels) == 0:
        raise ValueError(f"{model_name}: 레이블이 비어있습니다.")
    
    # 레이블 범위 검증
    if len(unique_labels) != n_clusters:
        raise ValueError(f"{model_name}: 예상 클러스터 수({n_clusters})와 실제 클러스터 수({len(unique_labels)})가 다릅니다.")
    
    # 빈 클러스터 검증
    empty_clusters = np.where(cluster_sizes == 0)[0]
    if len(empty_clusters) > 0:
        raise ValueError(f"{model_name}: 빈 클러스터가 있습니다: {empty_clusters}")
    
    # 레이블 연속성 검증
    if not np.array_equal(unique_labels, np.arange(n_clusters)):
        raise ValueError(f"{model_name}: 레이블이 0부터 {n_clusters-1}까지 연속적이지 않습니다.")
    
    logger.info(f"{model_name} 클러스터링 결과 검증 완료:")
    logger.info(f"- 총 데이터 수: {len(labels)}")
    logger.info(f"- 클러스터 수: {len(unique_labels)}")
    logger.info(f"- 각 클러스터 크기: {cluster_sizes}")
    
@measure_time
def find_optimal_k(features, k_range=range(4, 16), use_elbow=True):
    """
    K-means만 사용하여 최적 K 찾기 (Composite Score + 엘보우 기하 방식)
    :param features: 클러스터링에 사용할 특성 벡터 (numpy 배열)
    :param k_range: 탐색할 K 범위
    :param use_elbow: True이면 엘보우 기법으로 K를 결정 (직선과의 최대 거리)
    """
    logger.info(f"\n{'='*20} K-means로 최적 K 탐색 {'='*20}")
    logger.info(f"데이터 형태: {features.shape}")
    logger.info(f"K 범위: {k_range.start}~{k_range.stop-1}")
    
    k_scores = []     # Composite Score 리스트
    sse_list = []     # SSE(Inertia) 리스트
    cluster_sizes_history = []
    
    for k in k_range:
        try:
            logger.info(f"\n[K={k}] 검증 시작...")
            
            # K-means 실행
            kmeans = KMeans(
                n_clusters=k,
                init='k-means++',
                n_init=15,  # 초기화 횟수 조정 가능
                random_state=CONFIG['RANDOM_SEED']
            )
            labels = kmeans.fit_predict(features)
            
            # SSE(Inertia) 기록
            sse_list.append(kmeans.inertia_)
            
            # 클러스터 크기 확인
            cluster_sizes = np.bincount(labels)
            cluster_sizes_history.append(dict(enumerate(cluster_sizes)))
            logger.info(f"클러스터 크기 분포: {dict(enumerate(cluster_sizes))}")
            
            # 복합 점수 계산
            scores = compute_composite_score(features, labels)
            k_scores.append(scores['composite'])
            
            logger.info(f"K={k} 결과:")
            logger.info(f"- SSE(Inertia): {kmeans.inertia_:.4f}")
            logger.info(f"- Silhouette: {scores['silhouette']:.4f}")
            logger.info(f"- Composite Score: {scores['composite']:.4f}")
        
        except Exception as e:
            logger.error(f"K={k} 검증 실패: {str(e)}")
            k_scores.append(0.0)
            sse_list.append(float('inf'))
            continue
    
    # ---------------------------
    # (1) 엘보우 기하 방식으로 K 찾기
    # ---------------------------
    if use_elbow and len(k_range) >= 2:
        logger.info("\n엘보우 포인트(직선 최대 거리) 방식으로 최적 K 찾기...")
        elbow_k = _get_elbow_point(k_range, sse_list)
        
        if elbow_k is not None:
            logger.info(f"엘보우 방식으로 결정된 K: {elbow_k}")
            return elbow_k
        else:
            logger.warning("엘보우 포인트 탐색 실패 → Composite Score 기반으로 결정")
    
    # ---------------------------
    # (2) Composite Score 방식
    # ---------------------------
    best_k = k_range[np.argmax(k_scores)]
    logger.info(f"\n최적 K 결정: {best_k} (Composite Score 최대)")
    logger.info(f"최종 클러스터 크기: {cluster_sizes_history[np.argmax(k_scores)]}")
    return best_k


def _get_elbow_point(k_range, sse_list):
    """
    (k_min, SSE_min) ~ (k_max, SSE_max)를 잇는 직선으로부터
    각 (k, SSE)에 대한 '수직 거리'를 구해,
    그 거리가 가장 큰 K 값을 반환.
    """
    # 시작점(p1)과 끝점(p2)을 설정
    p1 = (k_range[0], sse_list[0])
    p2 = (k_range[-1], sse_list[-1])
    
    # p1과 p2가 같은 값이면(이상치) elbow 포인트를 구할 수 없음
    if p1 == p2:
        return None
    
    # 벡터 p1->p2
    p1p2 = (p2[0] - p1[0], p2[1] - p1[1])
    p1p2_length = math.sqrt(p1p2[0]**2 + p1p2[1]**2)
    
    max_dist = -1.0
    elbow_k = None
    
    # k_range와 sse_list 순회하며 최대 거리 지점 찾기
    for k, sse in zip(k_range, sse_list):
        # 현재 점 p = (k, sse)
        # p1->p 벡터
        p1p = (k - p1[0], sse - p1[1])
        
        # 외적(크로스) = |p1p x p1p2|
        # 2D 외적의 크기 = |x1*y2 - y1*x2|
        cross_val = abs(p1p[0]*p1p2[1] - p1p[1]*p1p2[0])
        
        # p1p2 길이로 나누면 p가 p1p2 직선에서 떨어진 '수직 거리'
        dist = cross_val / p1p2_length
        
        if dist > max_dist:
            max_dist = dist
            elbow_k = k
    
    return elbow_k

@measure_time
def process_couple_features(couples_df: pd.DataFrame, user_features: np.ndarray, users_df: pd.DataFrame):
    """
    커플 특성 처리 및 거리 계산
    
    Parameters:
        couples_df: pd.DataFrame - 커플 데이터프레임
        user_features: np.ndarray - 유저 특성 행렬
        users_df: pd.DataFrame - 사용자 데이터프레임
    
    Returns:
        pd.DataFrame: 거리 정보가 추가된 커플 데이터프레임
    """
    # 1. 유저 간 거리 계산
    user_distances = calculate_user_distances(couples_df, user_features, users_df)
    
    # 2. 커플 데이터프레임에 거리 정보 추가
    couples_df = couples_df.copy()
    couples_df['user_cluster_distance'] = user_distances
    
    # 3. 결혼 날짜 순환 특성 추가
    cyclic_features = couples_df['marriage_date'].apply(encode_cyclic_features)
    couples_df['marriage_month_sin'] = [x[0] for x in cyclic_features]
    couples_df['marriage_month_cos'] = [x[1] for x in cyclic_features]
    couples_df['marriage_day_sin'] = [x[2] for x in cyclic_features]
    couples_df['marriage_day_cos'] = [x[3] for x in cyclic_features]
    
    # 4. 나이 차이 계산
    couples_df['age_diff'] = couples_df['male_age'] - couples_df['female_age']
    
    return couples_df

@measure_time
def evaluate_user_clustering(df: pd.DataFrame) -> dict:
    """유저 클러스터링 평가 (MBTI 결측 처리 추가)"""
    config = CONFIG['USER_CLUSTERS']
    features, feature_names, scalers = prepare_clustering_features(df, config, 'user')
    
    # MBTI 결측 비율 확인
    mbti_features = features[:, 2:6]  # MBTI 관련 특성
    mbti_missing = np.all(mbti_features == -1, axis=1)
    mbti_missing_ratio = np.mean(mbti_missing)
    
    # MBTI 결측이 많은 경우 가중치 조정
    if mbti_missing_ratio > 0.3:
        logger.warning(f"Adjusting MBTI weights due to high missing ratio: {mbti_missing_ratio:.2%}")
        config['WEIGHT_RANGES'].update({
            'mbti_ie': (0.1, 3.0),
            'mbti_sn': (0.1, 3.0),
            'mbti_tf': (0.1, 3.0),
            'mbti_jp': (0.1, 3.0)
        })
    
    optimized_weights = optimize_user_weights(features)

    weighted_features = features * optimized_weights['weights']
    results = evaluate_models(weighted_features, optimized_weights['optimal_k'])
    best_model_name, best_model_data = max(results.items(), key=lambda x: x[1]['scores']['composite'])

    return {
        'model_type': best_model_name,
        'model': best_model_data['model'],
        'labels': best_model_data['labels'],
        'scores': best_model_data['scores'],
        'feature_names': feature_names,
        'scalers': scalers,
        'weights': optimized_weights,
        'weighted_features': weighted_features
    }

@measure_time
def evaluate_couple_clustering(df: pd.DataFrame,
                               user_features: np.ndarray,
                               users_df: pd.DataFrame) -> dict:
    """
    커플 클러스터링 전용
    """
    config = CONFIG['COUPLE_CLUSTERS']
    
    # 커플-사용자 distance 계산
    df = process_couple_features(df, user_features, users_df)

    features, feature_names, scalers = prepare_clustering_features(df, config, 'couple')
    optimized_weights = optimize_couple_weights(features)

    weighted_features = features * optimized_weights['weights']
    results = evaluate_models(weighted_features, optimized_weights['optimal_k'])
    best_model_name, best_model_data = max(results.items(), key=lambda x: x[1]['scores']['composite'])

    return {
        'model_type': best_model_name,
        'model': best_model_data['model'],
        'labels': best_model_data['labels'],
        'scores': best_model_data['scores'],
        'feature_names': feature_names,
        'scalers': scalers,
        'weights': optimized_weights,
        'weighted_features': weighted_features
    }

# ---------------------------
# 12. 결과 저장 함수들
# ---------------------------

@measure_time
def validate_data(df: pd.DataFrame, required_columns: list, cluster_type: str):
    """
    데이터 검증 (필수 컬럼만 체크하도록 수정)
    """
    # MBTI를 제외한 필수 컬럼 체크
    essential_columns = [col for col in required_columns if not col.endswith('mbti')]
    missing_cols = [col for col in essential_columns if col not in df.columns]
    
    if missing_cols:
        raise ValueError(f"{cluster_type} 클러스터링에 필요한 필수 컬럼이 누락되었습니다: {missing_cols}")
    
    # 데이터 타입 검증
    for col in df.columns:
        if col.endswith('_age') and not pd.api.types.is_numeric_dtype(df[col]):
            raise ValueError(f"나이 컬럼({col})이 숫자 타입이 아닙니다.")
        elif col == 'budget' and not pd.api.types.is_numeric_dtype(df[col]):
            raise ValueError("예산(budget) 컬럼이 숫자 타입이 아닙니다.")
        # MBTI는 있는 경우에만 검증
        elif col.endswith('_mbti') and col in df.columns:
            if not pd.api.types.is_string_dtype(df[col]) and not pd.isna(df[col]).all():
                raise ValueError(f"MBTI 컬럼({col})이 문자열 타입이 아닙니다.")

@measure_time
def clean_numeric_data(df, numeric_columns):
    """수치형 데이터 정제"""
    for col in numeric_columns:
        df[col] = pd.to_numeric(df[col], errors='coerce')
        if df[col].isna().any():
            median_val = df[col].median()
            df[col].fillna(median_val, inplace=True)
            logger.warning(f"Filled {df[col].isna().sum()} missing {col} values with median: {median_val}")
    return df

@measure_time
def assign_user_clusters(df: pd.DataFrame, user_labels: np.ndarray, users_df: pd.DataFrame):
    """
    유저 클러스터 정보를 커플 데이터에 추가 (ID 매핑 사용)
    """
    # 사용자 ID와 클러스터 매핑 생성
    user_clusters = pd.Series(user_labels, index=users_df['user_id'])
    
    # 매핑을 사용하여 클러스터 할당
    df['female_cluster'] = df['female_user_id'].map(user_clusters)
    df['male_cluster'] = df['male_user_id'].map(user_clusters)
    
    return df

@measure_time
def calculate_user_distances(df: pd.DataFrame, user_features: np.ndarray, users_df: pd.DataFrame):
    """
    유저 간 거리 계산 (ID 매핑 방식 사용 + 예외처리 추가)
    """
    try:
        # 사용자 ID를 인덱스로 하는 특성 매핑 생성
        user_features_map = pd.Series(
            [tuple(features) for features in user_features],
            index=users_df['user_id']
        )
        
        # 각 커플의 female과 male 특성 가져오기 (예외처리 추가)
        female_features = []
        male_features = []
        
        for _, row in df.iterrows():
            try:
                female_feat = user_features_map[row['female_user_id']]
                male_feat = user_features_map[row['male_user_id']]
            except KeyError as e:
                logger.warning(f"사용자 ID {e}가 매핑되지 않았습니다. 기본값으로 대체합니다.")
                # 기본값으로 평균 특성 사용
                default_features = tuple(np.mean(user_features, axis=0))
                female_feat = user_features_map.get(row['female_user_id'], default_features)
                male_feat = user_features_map.get(row['male_user_id'], default_features)
            
            female_features.append(female_feat)
            male_features.append(male_feat)
        
        female_features = np.array(female_features)
        male_features = np.array(male_features)
        
        # 유클리드 거리 계산
        distances = np.linalg.norm(female_features - male_features, axis=1)
        return distances
        
    except Exception as e:
        logger.error(f"거리 계산 중 오류 발생: {str(e)}")
        raise

@measure_time
def get_cluster_centers(model, model_type: str, X: np.ndarray = None) -> np.ndarray:
    """
    모델 유형에 따라 적절한 클러스터 중심점 반환
    
    Parameters:
        model: 클러스터링 모델 객체
        model_type: str - 모델 유형 ('kmeans', 'gmm', 'hierarchical')
        X: np.ndarray - 클러스터링에 사용된 원본 특성 행렬 (hierarchical 클러스터링용)
    
    Returns:
        np.ndarray: 클러스터 중심점
    """
    if model_type == 'kmeans':
        return model.cluster_centers_
    elif model_type == 'gmm':
        return model.means_
    elif model_type == 'hierarchical':
        if X is None:
            raise ValueError("Hierarchical clustering requires original feature matrix X")
        labels = model.labels_
        n_clusters = len(np.unique(labels))
        n_features = X.shape[1]
        centers = np.zeros((n_clusters, n_features))
        for i in range(n_clusters):
            mask = labels == i
            if np.any(mask):
                centers[i] = np.mean(X[mask], axis=0)
        return centers
    else:
        raise ValueError(f"Unsupported model type: {model_type}")

@measure_time
def save_extended_clustering_outputs(
    user_results: dict,
    couple_results: dict,
    users_df: pd.DataFrame,
    couples_df: pd.DataFrame
):
    """
    5개 JSON 파일 생성 (가중치가 적용된 특성값 포함):
      1) clustering_summary.json
      2) user_cluster_centers.json
      3) couple_cluster_centers.json
      4) user_assignments.json (배열 형태로 [{ "userid": X, "cluster": Y, "weighted_features": [...] }, ...])
      5) couple_assignments.json (배열 형태로 [{ "coupleid": X, "cluster": Y, "weighted_features": [...] }, ...])
    """
    try:
        logger.info("[저장] 시작")

        # ----------------------------------------------------
        # 1) 통합 리포트: clustering_summary.json
        # ----------------------------------------------------
        summary_data = {
            "timestamp": time.strftime('%Y-%m-%d %H:%M:%S'),
            "user": {
                "model_type": user_results["model_type"],
                "scores": user_results["scores"],     
                "weights": dict(zip(
                    user_results["feature_names"],
                    user_results["weights"]["weights"].tolist()
                    if isinstance(user_results["weights"]["weights"], np.ndarray)
                    else user_results["weights"]["weights"]
                ))
            },
            "couple": {
                "model_type": couple_results["model_type"],
                "scores": couple_results["scores"],
                "weights": dict(zip(
                    couple_results["feature_names"],
                    couple_results["weights"]["weights"].tolist()
                    if isinstance(couple_results["weights"]["weights"], np.ndarray)
                    else couple_results["weights"]["weights"]
                ))
            }
        }

        # ----------------------------------------------------
        # 2) 사용자 클러스터 중심점: user_cluster_centers.json
        # ----------------------------------------------------
        user_centers_np = get_cluster_centers(
            user_results['model'],
            user_results['model_type'],
            X=user_results['weighted_features']
        )
        user_feature_names = user_results["feature_names"]

        user_centers_dict = {}
        for i, center in enumerate(user_centers_np):
            center_dict = {}
            for fname, fval in zip(user_feature_names, center):
                center_dict[fname] = float(fval)
            user_centers_dict[f"cluster_{i}"] = center_dict

        # ----------------------------------------------------
        # 3) 커플 클러스터 중심점: couple_cluster_centers.json
        # ----------------------------------------------------
        couple_centers_np = get_cluster_centers(
            couple_results['model'],
            couple_results['model_type'],
            X=couple_results['weighted_features']
        )
        couple_feature_names = couple_results["feature_names"]

        couple_centers_dict = {}
        for i, center in enumerate(couple_centers_np):
            center_dict = {}
            for fname, fval in zip(couple_feature_names, center):
                center_dict[fname] = float(fval)
            couple_centers_dict[f"cluster_{i}"] = center_dict

        # ----------------------------------------------------
        # 4) 사용자 클러스터 할당: user_assignments.json
        # ----------------------------------------------------
        user_labels = user_results['model'].labels_
        weighted_user_features = user_results['weighted_features']
        
        user_assignments = []
        for i, user_id in enumerate(users_df['user_id']):
            user_assignments.append({
                "userid": int(user_id),
                "cluster": int(user_labels[i]),
                "weighted_features": weighted_user_features[i].tolist()
            })

        # ----------------------------------------------------
        # 5) 커플 클러스터 할당: couple_assignments.json
        # ----------------------------------------------------
        couple_labels = couple_results['model'].labels_
        weighted_couple_features = couple_results['weighted_features']
        
        couple_assignments = []
        for i, couple_id in enumerate(couples_df['couple_id']):
            couple_assignment = {
                "coupleid": int(couple_id),
                "cluster": int(couple_labels[i]),
                "weighted_features": weighted_couple_features[i].tolist()
            }
            
            couple_assignments.append(couple_assignment)

        # 파일 저장
        resource_dir = CONFIG['RESOURCE_DIR']
        
        with open(resource_dir / 'clustering_summary.json', 'w', encoding='utf-8') as f:
            json.dump(summary_data, f, indent=2, ensure_ascii=False)
            
        with open(resource_dir / 'user_cluster_centers.json', 'w', encoding='utf-8') as f:
            json.dump(user_centers_dict, f, indent=2, ensure_ascii=False)
            
        with open(resource_dir / 'couple_cluster_centers.json', 'w', encoding='utf-8') as f:
            json.dump(couple_centers_dict, f, indent=2, ensure_ascii=False)
            
        with open(resource_dir / 'user_assignments.json', 'w', encoding='utf-8') as f:
            json.dump(user_assignments, f, indent=2, ensure_ascii=False)
            
        with open(resource_dir / 'couple_assignments.json', 'w', encoding='utf-8') as f:
            json.dump(couple_assignments, f, indent=2, ensure_ascii=False)

        logger.info("클러스터링 결과 파일 저장 완료")
        
        # 결과 검증
        logger.info(f"저장된 유저 수: {len(user_assignments)}")
        logger.info(f"저장된 커플 수: {len(couple_assignments)}")
        
        # 샘플 데이터 로깅
        if user_assignments:
            logger.info(f"유저 샘플 데이터:\n{user_assignments[0]}")
        if couple_assignments:
            logger.info(f"커플 샘플 데이터:\n{couple_assignments[0]}")

    except Exception as e:
        logger.error(f"결과 저장 중 오류 발생: {str(e)}")
        raise

# ---------------------------
# 13. 메인 실행 함수
# ---------------------------

@measure_time
def main(config=CONFIG):
    """메인 실행 함수"""
    try:
        logger.info("\n=== 웨딩 추천 시스템 클러스터링 시작 ===")
        
        # 1. 사용자 클러스터링
        logger.info("\n[1/2] 사용자 클러스터링 시작")
        features, scalers, users_df = preprocess_data(cluster_type='user')
        user_results = evaluate_user_clustering(users_df)
        
        # 2. 커플 클러스터링
        logger.info("\n[2/2] 커플 클러스터링 시작")
        features, scalers, couples_df = preprocess_data(cluster_type='couple')
        couple_results = evaluate_couple_clustering(
            df=couples_df,
            user_features=user_results['weighted_features'],
            users_df=users_df
        )
        
        # 결과 저장
        results = {
            'user_clustering': user_results,
            'couple_clustering': couple_results,
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S')
        }
        
        save_extended_clustering_outputs(
            user_results,
            couple_results,
            users_df,
            couples_df
        )

        # 새로 추가한 부분: 스케일러 파라미터를 JSON 형태로 저장
        save_scaler_params(user_results['scalers'], 'user')
        save_scaler_params(couple_results['scalers'], 'couple')

        logger.info("\n=== 클러스터링 완료 ===")
        return results
        
    except Exception as e:
        logger.error(f"\n❌ 오류 발생: {str(e)}")
        raise

@measure_time
def save_scaler_params(scalers: dict, cluster_type: str):
    """
    각 특성에 적용된 스케일러 파라미터를 JSON 파일로 저장.
    스프링 등 외부 시스템에서 동일한 전처리를 재현할 수 있도록 보조한다.
    """
    try:
        output_file = (
            CONFIG['RESOURCE_DIR'] / f"{cluster_type}_scaler_params.json"
        )

        # scalers: {"age": StandardScaler(), "budget": MinMaxScaler(), ...} 형식 가정
        scaler_data = {}
        for feature_name, scaler in scalers.items():
            if scaler is None:
                # MBTI 등 스케일링하지 않는 특성은 None
                scaler_data[feature_name] = None
            else:
                params = {}
                if hasattr(scaler, 'mean_'):
                    params['mean_'] = scaler.mean_.tolist()
                if hasattr(scaler, 'var_'):
                    params['var_'] = scaler.var_.tolist()
                if hasattr(scaler, 'scale_'):
                    params['scale_'] = scaler.scale_.tolist()
                if hasattr(scaler, 'min_'):
                    params['min_'] = scaler.min_.tolist()
                if hasattr(scaler, 'data_min_'):
                    params['data_min_'] = scaler.data_min_.tolist()
                if hasattr(scaler, 'data_max_'):
                    params['data_max_'] = scaler.data_max_.tolist()
                scaler_data[feature_name] = params

        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(scaler_data, f, indent=2, ensure_ascii=False)

        logger.info(f"{cluster_type}_scaler_params.json 저장 완료: {output_file}")
    except Exception as e:
        logger.error(f"스케일러 파라미터 저장 실패 ({cluster_type}): {str(e)}")
        raise

if __name__ == "__main__":
    try:
        # 커맨드 라인 인자 처리
        if len(sys.argv) > 1:
            CONFIG['USE_DUMMY_DATA'] = sys.argv[1].lower() == 'true'
        if len(sys.argv) > 2:
            CONFIG['INPUT_FILE_PATH'] = sys.argv[2]
        
        # 결과 디렉토리 생성
        os.makedirs(CONFIG['RESOURCE_DIR'], exist_ok=True)
        
        # 메인 실행
        main(CONFIG)
        
    except Exception as e:
        logger.error(f"Script execution failed: {str(e)}")
        sys.exit(1)





