"""
[Wedding Recommender System - Clustering Module]

스프링 연동 프로세스:
--------------------------------
1. 초기/주기적 클러스터링 (관리자/배치)
    - 전체 커플 사용자 데이터로 클러스터링 수행
    - JSON 형식의 모델 파라미터 파일 생성
    - 저장 위치: ${json.output.dir} (프로젝트루트/data/resources)
        ㄴ clustering_summary.json (클러스터링 결과 요약)
        ㄴ user_cluster_centers.json (사용자 클러스터 중심점)
        ㄴ couple_cluster_centers.json (커플 클러스터 중심점)
        ㄴ user_assignments.json (사용자별 클러스터 할당)
        ㄴ couple_assignments.json (커플별 클러스터 할당)
        ㄴ user_scaler_params.json (사용자 스케일러 파라미터)
        ㄴ couple_scaler_params.json (커플 스케일러 파라미터)

2. 신규 사용자 분류 (스프링)
    - JSON 파일들을 읽어 필요한 파라미터 로드
    - 입력 데이터 정규화 (스케일러 파라미터 사용)
    - 거리 계산으로 가장 가까운 클러스터 할당

클러스터링 프로세스:
--------------------------------
1. 데이터 전처리
    - JSON 데이터 로드 및 NumPy 배열 변환
    - 특성 스케일링 (StandardScaler/MinMaxScaler)
    - MBTI 인코딩, 날짜 순환 인코딩

2. 최적 K 결정 (Elbow Method)
    - SSE(Sum of Squared Errors) 계산
    - 엘보우 포인트 탐색
    - 시각화 결과 저장 (optional)

3. 클러스터링 모델 평가
    - K-means, GMM, Hierarchical 클러스터링
    - 평가 지표:
        * Silhouette Score(40%)
        * Davies-Bouldin Score(20%)
        * Calinski-Harabasz Score(20%)
        * Dunn Index(20%)

4. 가중치 최적화 (Bayesian Optimization)
    - 사용자 클러스터링 가중치:
        * age_weight: [2.0, 10.0]
        * budget_weight: [4.0, 16.0]
        * mbti_weights: [0.5, 8.0]
    
    - 커플 클러스터링 가중치:
        * age_weights: [2.0, 8.0]
        * budget_weight: [4.0, 16.0]
        * date_weights: [0.1, 5.0]
        * distance_weight: [0.5, 6.0]

입력 데이터 (JSON):
--------------------------------
{
    "couples": [
        {
            "couple_id": int,
            "female": {
                "user_id": int,
                "age": int,
                "mbti": str
            },
            "male": {
                "user_id": int,
                "age": int,
                "mbti": str
            },
            "budget": int,
            "marriage_date": str
        },
        ...
    ]
}

특성 처리:
--------------------------------
1. 사용자 특성
    - 나이: StandardScaler
    - 예산: MinMaxScaler
    - MBTI: 4차원 이진 인코딩 [I/E, S/N, T/F, J/P]

2. 커플 특성
    - 나이: StandardScaler (남/여)
    - 예산: MinMaxScaler
    - 결혼날짜: 순환 인코딩 (sin/cos)
    - 나이차: MinMaxScaler
    - 유저 클러스터 거리: MinMaxScaler

병렬 처리:
--------------------------------
- CPU 코어 수 - 1개의 worker 사용
- 모델 평가/K 탐색 등에 병렬 처리 적용
- joblib의 Parallel/delayed 사용

설정 관리:
--------------------------------
- CONFIG 딕셔너리로 중앙 집중식 설정
- 환경변수 RESOURCE_DIR로 출력 경로 제어
- 더미데이터 생성 옵션 (USE_DUMMY_DATA)
- K 탐색/가중치 최적화 스킵 옵션

로깅:
--------------------------------
- 파일/콘솔 동시 출력
- 타임스탬프 포함 상세 로그
- 주요 단계별 소요시간 측정
"""

import os
import sys
import json
import time
import math
import logging
import numpy as np
from pathlib import Path
from functools import wraps
from sklearn.preprocessing import StandardScaler, MinMaxScaler
from sklearn.cluster import KMeans, AgglomerativeClustering
from sklearn.mixture import GaussianMixture
from sklearn.metrics import (
    silhouette_score,
    davies_bouldin_score,
    calinski_harabasz_score
)
from skopt import gp_minimize
from skopt.space import Real
from skopt.utils import use_named_args
from scipy.spatial.distance import cdist, pdist
from kneed import KneeLocator
import matplotlib.pyplot as plt
from joblib import Parallel, delayed
import multiprocessing
from functools import partial
from sklearn.decomposition import PCA
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.colors as mcolors


############################################
# 설정 CONFIG
############################################
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_RESOURCE_DIR = SCRIPT_DIR.parent.parent.parent.parent / 'data' / 'resources'

CONFIG = {
    'USE_DUMMY_DATA': False,           # True: 더미데이터 / False: input_data.json 사용
    'SAMPLE_SIZE': 10000,             # 더미 커플 생성 시 개수
    'RESOURCE_DIR': Path(os.getenv('RESOURCE_DIR', str(DEFAULT_RESOURCE_DIR))),
    'RANDOM_SEED': 42,

    # * 추가: K Range 설정 (유저, 커플 모두 동일한 x-range 사용 원한다면 이렇게 동일하게 세팅)
    'USER_K_RANGE': range(2, 31),      # 사용자 K 탐색 구간 (2~20)
    'COUPLE_K_RANGE': range(2, 31),    # 커플 K 탐색 구간 (2~20)

    # 사용자 클러스터링 관련 설정
    'USER_CLUSTERS': {
        'FEATURES': [
            'age','budget','mbti_ie','mbti_sn','mbti_tf','mbti_jp'
        ],
        'N_CLUSTERS': 9,  # K 최적화를 건너뛰는 경우 사용할 기본 K
        'WEIGHT_RANGES': {
            'age': (2.0, 10.0),
            'budget': (4.0, 16.0),
            'mbti_ie': (0.5, 8.0),
            'mbti_sn': (0.5, 8.0),
            'mbti_tf': (0.5, 8.0),
            'mbti_jp': (0.5, 8.0)
        }
    },

    # 커플 클러스터링 설정
    'COUPLE_CLUSTERS': {
        'FEATURES': [
            'male_age','female_age','budget',
            'marriage_month_sin','marriage_month_cos',
            'marriage_day_sin','marriage_day_cos',
            'age_diff','user_cluster_distance'
        ],
        'N_CLUSTERS': 9,  # K 최적화를 건너뛰는 경우 사용할 기본 K
        'WEIGHT_RANGES': {
            'male_age': (2.0, 8.0),
            'female_age': (2.0, 8.0),
            'budget': (4.0, 16.0),
            'marriage_month_sin': (0.5, 5.0),
            'marriage_month_cos': (0.5, 5.0),
            'marriage_day_sin': (0.1, 1.0),
            'marriage_day_cos': (0.1, 1.0),
            'age_diff': (0.5, 12.0),
            'user_cluster_distance': (0.5, 6.0)
        }
    },

    # 가중치 최적화(Feature Weights) 관련
    'OPTIMIZE_WEIGHTS': False,
    'OPTIMIZE_N_CALLS': 50,
    'OPTIMIZATION_N_RANDOM_STARTS': 15,

    # * 추가: Elbow 그래프(무릎점) 시각화 on/off 설정
    'PLOT_ELBOW_GRAPHS': False,

    # * 추가: K 탐색(최적 K 찾기) 과정 스킵 on/off
    'SKIP_OPTIMIZE_K': True,

    # 파일명
    'USER_WEIGHTS_FILE': 'user_optimized_weights.json',
    'COUPLE_WEIGHTS_FILE': 'couple_optimized_weights.json',
    'N_JOBS': max(1, multiprocessing.cpu_count() - 1),  # CPU 코어 수 - 1
    'PARALLEL_BACKEND': 'loky',  # 'loky', 'multiprocessing', 'threading' 중 선택

    # 3D 시각화 관련 설정
    'VISUALIZATION': {
        'ENABLED': True,                # 3D 시각화 활성화 여부
        'FIGURE_SIZE': (12, 8),         # 그래프 크기
        'DPI': 300,                     # 저장 해상도
        'POINT_SIZE': 50,               # 데이터 포인트 크기
        'CENTROID_SIZE': 200,           # 중심점 크기
        'ALPHA': 0.6,                   # 투명도
        'USE_PCA': True,                # PCA 압축 사용 여부
        'USER_FEATURES': {              # 사용자 시각화 설정
            'title': 'User Clusters',
            'filename': 'user_clusters_3d.png'
        },
        'COUPLE_FEATURES': {            # 커플 시각화 설정
            'title': 'Couple Clusters',
            'filename': 'couple_clusters_3d.png'
        }
    },
}

# 기본 가중치
DEFAULT_WEIGHTS = {
    'user': {
        'weights': np.array([4.0,7.0,1.0,1.0,1.0,1.0]),
        'optimal_k': 9,
        'score': 0.0
    },
    'couple': {
        'weights': np.array([3.0,3.0,6.0,2.0,2.0,2.0,2.0,2.0,2.0]),
        'optimal_k': 9,
        'score': 0.0
    }
}


############################################
# 로깅 설정
############################################
def setup_logging():
    log_dir = CONFIG['RESOURCE_DIR'] / 'logs'
    log_dir.mkdir(parents=True, exist_ok=True)
    
    log_file = log_dir / f'clustering_{time.strftime("%Y%m%d_%H%M%S")}.log'
    
    # 파일 핸들러
    file_handler = logging.FileHandler(log_file, encoding='utf-8')
    file_handler.setFormatter(logging.Formatter(
        '%(asctime)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    ))
    
    # 콘솔 핸들러
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(logging.Formatter(
        '%(asctime)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    ))
    
    # 로거 설정
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.INFO)
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    return logger

logger = setup_logging()

############################################
# 시간 측정용 데코레이터
############################################
def measure_time(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = time.time()
        result = func(*args, **kwargs)
        elapsed = time.time() - start_time
        logger.info(f"[TIME] {func.__name__} 완료 (소요: {elapsed:.2f}초)")
        return result
    return wrapper


############################################
# 더미 데이터 생성 (새로운 단일 함수)
############################################
@measure_time
def create_dummy_couples_json(n_couples=2000):
    """
    테스트용 더미 커플 데이터 생성
    """
    np.random.seed(CONFIG['RANDOM_SEED'])
    couples_data = {"couples": []}

    # MBTI 실제 분포 (한국 기준 통계)
    mbti_types = [
        'ISTJ', 'ISFJ', 'INFJ', 'INTJ',
        'ISTP', 'ISFP', 'INFP', 'INTP',
        'ESTP', 'ESFP', 'ENFP', 'ENTP',
        'ESTJ', 'ESFJ', 'ENFJ', 'ENTJ'
    ]
    
    # 실제 한국인 MBTI 분포 비율 (%)
    mbti_weights = np.array([
        0.135, 0.130, 0.020, 0.025,  # ISTJ, ISFJ, INFJ, INTJ (31%)
        0.085, 0.080, 0.040, 0.030,  # ISTP, ISFP, INFP, INTP (23.5%)
        0.035, 0.085, 0.080, 0.030,  # ESTP, ESFP, ENFP, ENTP (23%)
        0.100, 0.095, 0.020, 0.010   # ESTJ, ESFJ, ENFJ, ENTJ (22.5%)
    ])
    
    # 가중치 정규화 (합이 1이 되도록)
    mbti_weights = mbti_weights / mbti_weights.sum()
    
    # 나이 그룹 정의 (더 균일한 구분)
    age_groups = [
        (25, 2),  # (평균, 표준편차) - 20대 초중반
        (29, 2),  # 20대 후반
        (33, 2),  # 30대 초중반
        (37, 2),  # 30대 후반
        (42, 2)   # 40대 초반
    ]

    # 예산 그룹 정의 (더 균일한 구분)
    budget_groups = [
        (3000, 500),    # 저예산
        (5000, 500),    # 중저예산
        (7000, 500),    # 중예산
        (9000, 500),    # 중고예산
        (11000, 500)    # 고예산
    ]

    for i in range(n_couples):
        age_group = age_groups[i % len(age_groups)]
        budget_group = budget_groups[i % len(budget_groups)]
        
        # 나이 생성
        female_age = int(np.clip(
            np.random.normal(age_group[0]-2, age_group[1]), 
            20, 45
        ))
        male_age = int(np.clip(
            np.random.normal(age_group[0], age_group[1]), 
            22, 47
        ))
        
        # 예산 생성 (원 단위)
        budget = int(np.clip(
            np.random.normal(budget_group[0], budget_group[1]), 
            2000, 12000
        )) * 10000

        # MBTI - 실제 분포 기반 선택
        female_mbti = np.random.choice(mbti_types, p=mbti_weights)
        male_mbti = np.random.choice(mbti_types, p=mbti_weights)

        # 결혼 날짜 (시즌성 반영)
        month = np.random.choice(range(1,13), p=[
            0.04, 0.04,
            0.09, 0.09, 0.09,
            0.09, 0.09, 0.09, 
            0.14, 0.14, 0.09,
            0.01  
        ])
        day = np.random.randint(1, 29)

        couple_dict = {
            "couple_id": int(i + 1),
            "female": {
                "user_id": int((i * 2) + 1),
                "age": female_age,
                "mbti": female_mbti
            },
            "male": {
                "user_id": int((i * 2) + 2),
                "age": male_age,
                "mbti": male_mbti
            },
            "budget": budget,
            "marriage_date": f"{month:02d}-{day:02d}"
        }
        couples_data["couples"].append(couple_dict)

    logger.info(f"[create_dummy_couples_json] 데이터 생성 완료: total={n_couples}")
    return couples_data

############################################
# 유틸 함수
############################################
def mbti_to_encoding(mbti_str):
    """
    MBTI 문자열을 이진 벡터로 인코딩
    
    Args:
        mbti_str (str): 'ISTJ'와 같은 4글자 MBTI 문자열
    
    Returns:
        tuple: (ie, sn, tf, jp) 형태의 이진값 튜플
            - I/E: 0/1
            - S/N: 0/1
            - T/F: 0/1
            - J/P: 0/1
        잘못된 입력의 경우 (0,0,0,0) 반환
    """
    if not mbti_str or len(mbti_str) != 4:
        return (0,0,0,0)

    mbti_str = mbti_str.upper()
    return (
        1 if mbti_str[0] == 'E' else 0,
        1 if mbti_str[1] == 'N' else 0,
        1 if mbti_str[2] == 'F' else 0,
        1 if mbti_str[3] == 'P' else 0
    )

def encode_cyclic_date(date_str):
    """
    날짜의 순환성을 보존하는 sin/cos 인코딩
    
    Args:
        date_str (str): 'MM-DD' 형식의 날짜 문자열
    
    Returns:
        tuple: (month_sin, month_cos, day_sin, day_cos)
            - month: 1~12월을 2π 범위로 변환
            - day: 1~31일을 2π 범위로 변환
        오류 시 (0,1,0,1) 반환
    """
    try:
        m,d = date_str.split('-')
        m = int(m)
        d = int(d)
        m_sin = np.sin(2*np.pi*(m-1)/12)
        m_cos = np.cos(2*np.pi*(m-1)/12)
        d_sin = np.sin(2*np.pi*(d-1)/31)
        d_cos = np.cos(2*np.pi*(d-1)/31)
        return (m_sin,m_cos,d_sin,d_cos)
    except:
        return (0,1,0,1)

############################################
# JSON → NumPy 변환
############################################
def users_to_numpy(couples_json):
    """
    커플 JSON에서 사용자 특성 추출 및 NumPy 배열 변환
    
    Args:
        couples_json (dict): 커플 데이터 JSON
    
    Returns:
        tuple: (user_array, user_ids)
            - user_array: shape=(N,6) [age, budget, mbti_ie, mbti_sn, mbti_tf, mbti_jp]
            - user_ids: shape=(N,) 사용자 ID 배열
    """
    # 벡터화를 위한 사전 메모리 할당
    n_couples = len(couples_json['couples'])
    user_data = np.zeros((n_couples * 2, 6), dtype=float)
    user_ids = np.zeros(n_couples * 2, dtype=int)
    
    for i, c in enumerate(couples_json['couples']):
        # female
        f_mbti = mbti_to_encoding(c['female']['mbti'])
        user_data[i*2] = [
            c['female']['age'],
            c['budget'],
            f_mbti[0],
            f_mbti[1],
            f_mbti[2],
            f_mbti[3]
        ]
        user_ids[i*2] = c['female']['user_id']

        # male
        m_mbti = mbti_to_encoding(c['male']['mbti'])
        user_data[i*2+1] = [
            c['male']['age'],
            c['budget'],
            m_mbti[0],
            m_mbti[1],
            m_mbti[2],
            m_mbti[3]
        ]
        user_ids[i*2+1] = c['male']['user_id']

    return user_data, user_ids

def couples_to_numpy(couples_json):
    """
    커플 JSON에서 커플 특성 추출 및 NumPy 배열 변환
    
    Args:
        couples_json (dict): 커플 데이터 JSON
    
    Returns:
        tuple: (couple_array, couple_ids, female_ids, male_ids)
            - couple_array: shape=(N,9) 
              [male_age, female_age, budget, m_sin, m_cos, d_sin, d_cos, age_diff, user_dist]
            - couple_ids: shape=(N,) 커플 ID 배열
            - female_ids: shape=(N,) 여성 사용자 ID 배열
            - male_ids: shape=(N,) 남성 사용자 ID 배열
    """
    couple_data = []
    couple_ids = []
    female_ids = []
    male_ids = []
    for c in couples_json['couples']:
        couple_ids.append(c['couple_id'])
        female_ids.append(c['female']['user_id'])
        male_ids.append(c['male']['user_id'])

        # 날짜
        msin,mcos,dsin,dcos = encode_cyclic_date(c['marriage_date'])
        age_diff = c['male']['age'] - c['female']['age']

        couple_data.append([
            c['male']['age'],
            c['female']['age'],
            c['budget'],
            msin,
            mcos,
            dsin,
            dcos,
            age_diff,
            0.0  # user_cluster_distance는 나중에 계산
        ])

    return (
        np.array(couple_data, dtype=float),
        np.array(couple_ids, dtype=int),
        np.array(female_ids, dtype=int),
        np.array(male_ids, dtype=int)
    )

############################################
# 스케일링
############################################
def scale_user_features(user_array):
    """
    사용자 특성 스케일링
    
    Args:
        user_array: shape=(N,6) [age, budget, mbti_ie, mbti_sn, mbti_tf, mbti_jp]
    
    Returns:
        tuple: (scaled_array, scalers)
            - scaled_array: 스케일링된 특성 배열
            - scalers: {
                "age": StandardScaler(),
                "budget": MinMaxScaler(),
                "mbti_*": None  # MBTI는 스케일링 불필요
              }
    """
    scaled = user_array.copy()
    age_scaler = StandardScaler()
    budget_scaler = MinMaxScaler()

    # 나이(0) -> StandardScaler
    scaled[:, 0:1] = age_scaler.fit_transform(scaled[:, 0:1])
    # 예산(1) -> MinMaxScaler
    scaled[:, 1:2] = budget_scaler.fit_transform(scaled[:, 1:2])

    scalers = {
        "age": age_scaler,
        "budget": budget_scaler,
        # MBTI는 스케일링 없음
        "mbti_ie": None,
        "mbti_sn": None,
        "mbti_tf": None,
        "mbti_jp": None
    }
    return scaled, scalers

def scale_couple_features(couple_array):
    """
    커플 특성 스케일링
    
    Args:
        couple_array: shape=(N,9) 
            [male_age, female_age, budget, m_sin, m_cos, d_sin, d_cos, age_diff, user_dist]
    
    Returns:
        tuple: (scaled_array, scalers)
            - scaled_array: 스케일링된 특성 배열
            - scalers: {
                "male_age": StandardScaler(),
                "female_age": StandardScaler(),
                "budget": MinMaxScaler(),
                ...
              }
    """
    # 초기화
    scaled = couple_array.copy()

    male_scaler = StandardScaler()
    female_scaler = StandardScaler()
    budget_scaler = MinMaxScaler()
    msin_scaler = MinMaxScaler()
    mcos_scaler = MinMaxScaler()
    dsin_scaler = MinMaxScaler()
    dcos_scaler = MinMaxScaler()
    agediff_scaler = MinMaxScaler()
    userdist_scaler = MinMaxScaler()

    # 0: male_age -> StandardScaler
    scaled[:, 0:1] = male_scaler.fit_transform(scaled[:, 0:1])
    # 1: female_age -> StandardScaler
    scaled[:, 1:2] = female_scaler.fit_transform(scaled[:, 1:2])

    # 2: budget -> MinMaxScaler
    scaled[:, 2:3] = budget_scaler.fit_transform(scaled[:, 2:3])
    # 3: m_sin -> MinMaxScaler
    scaled[:, 3:4] = msin_scaler.fit_transform(scaled[:, 3:4])
    # 4: m_cos -> MinMaxScaler
    scaled[:, 4:5] = mcos_scaler.fit_transform(scaled[:, 4:5])
    # 5: d_sin -> MinMaxScaler
    scaled[:, 5:6] = dsin_scaler.fit_transform(scaled[:, 5:6])
    # 6: d_cos -> MinMaxScaler
    scaled[:, 6:7] = dcos_scaler.fit_transform(scaled[:, 6:7])
    # 7: age_diff -> MinMaxScaler
    scaled[:, 7:8] = agediff_scaler.fit_transform(scaled[:, 7:8])
    # 8: user_cluster_distance -> MinMaxScaler
    scaled[:, 8:9] = userdist_scaler.fit_transform(scaled[:, 8:9])

    scalers = {
        "male_age": male_scaler,
        "female_age": female_scaler,
        "budget": budget_scaler,
        "marriage_month_sin": msin_scaler,
        "marriage_month_cos": mcos_scaler,
        "marriage_day_sin": dsin_scaler,
        "marriage_day_cos": dcos_scaler,
        "age_diff": agediff_scaler,
        "user_cluster_distance": userdist_scaler
    }
    return scaled, scalers


############################################
# 최적 K (Elbow / Composite)
############################################
def compute_dunn_index(features, labels):
    """
    Dunn 지수 계산 (클러스터 품질 평가)
    
    클러스터 간 분리도와 클러스터 내 응집도의 비율
    높을수록 좋은 클러스터링을 의미
    
    Args:
        features: 특성 행렬
        labels: 클러스터 레이블
    
    Returns:
        float: Dunn 지수 값 (0 이상)
    """
    labs = np.unique(labels)
    if len(labs) < 2:
        return 0

    clusters = [np.where(labels==lab)[0] for lab in labs]
    if len(clusters) < 2:
        return 0

    min_inter = float('inf')
    max_intra = 0

    for i,idx_i in enumerate(clusters):
        pts_i = features[idx_i]
        if len(pts_i) > 1:
            dist_i = pdist(pts_i).max()
            if dist_i > max_intra:
                max_intra = dist_i

        for j in range(i+1, len(clusters)):
            pts_j = features[clusters[j]]
            dist_ij = cdist(pts_i, pts_j).min()
            if dist_ij < min_inter:
                min_inter = dist_ij

    if max_intra == 0:
        return 0
    return float(min_inter / max_intra)

def compute_composite_score(features, labels):
    """
    복합 평가 점수 계산
    
    여러 클러스터링 평가 지표를 가중 결합
    
    Args:
        features: 특성 행렬
        labels: 클러스터 레이블
    
    Returns:
        dict: {
            'composite': float,  # 종합 점수
            'silhouette': float,  # 실루엣 점수 (-1~1)
            'davies_bouldin': float,  # Davies-Bouldin 지수
            'calinski_harabasz': float,  # Calinski-Harabasz 지수
            'dunn_index': float  # Dunn 지수
        }
    """
    s = silhouette_score(features, labels)
    db = davies_bouldin_score(features, labels)
    ch = calinski_harabasz_score(features, labels)
    dunn = compute_dunn_index(features, labels)

    # 정규화
    s_norm = (s + 1) * 0.5
    db_norm = 1 / (db + 1)
    ch_norm = np.clip(ch / 10000, 0, 1)
    dunn_norm = np.clip(dunn, 0, 1)

    composite = 0.4 * s_norm + 0.2 * db_norm + 0.2 * ch_norm + 0.2 * dunn_norm
    return {
        'composite': composite,
        'silhouette': s,
        'davies_bouldin': db,
        'calinski_harabasz': ch,
        'dunn_index': dunn
    }

############################################
# (추가) 엘보우 그래프 시각화 함수
############################################
def plot_elbow_graph(
    k_range, 
    sse_list, 
    elbow_k, 
    cluster_type, 
    figsize=(8,6), 
    x_margin=0.5, 
    y_margin_ratio=0.10
):
    """
    k_range, SSE 리스트, 그리고 knee point를 이용해 엘보우 그래프를 저장합니다.
    - x_margin: X축 양쪽 여백
    - y_margin_ratio: Y축을 min~max 구간에서 이 비율만큼 상하로 추가 여유
    """
    plt.figure(figsize=figsize)
    
    # 그래프 그리기
    plt.plot(k_range, sse_list, 'o-', color='#2E86C1', linewidth=2, markersize=6, label='SSE')
    
    # elbow point(무릎점) 표시
    if elbow_k is not None and elbow_k in k_range:
        elbow_sse = sse_list[k_range.index(elbow_k)]
        plt.plot(elbow_k, elbow_sse, 'ro', markersize=10, label=f'Knee={elbow_k}')

    plt.title(f'Elbow Plot ({cluster_type})')
    plt.xlabel('Number of Clusters (k)')
    plt.ylabel('SSE')
    plt.legend()
    plt.grid(True, alpha=0.3)

    # X축 레이블을 k_range 전체로 표시
    plt.xticks(k_range)

    # X축 범위에 x_margin 추가
    plt.xlim([k_range[0] - x_margin, k_range[-1] + x_margin])

    # Y축 범위: SSE 값의 최솟값, 최댓값에서 y_margin_ratio만큼 상하 추가
    ymin, ymax = min(sse_list), max(sse_list)
    margin = (ymax - ymin) * y_margin_ratio
    plt.ylim(ymin - margin, ymax + margin)

    # 그래프 저장
    output_name = CONFIG['RESOURCE_DIR'] / f"elbow_plot_{cluster_type}.png"
    plt.savefig(output_name, dpi=120, bbox_inches='tight')
    plt.close()
    logger.info(f"[plot_elbow_graph] 엘보우 그래프 저장 완료 => {output_name}")


def custom_elbow_by_threshold(k_range, sse_list, min_drop_ratio=0.08):
    """
    SSE 변화율을 이용해 "충분히 완만해졌다"고 판단하는 K를 찾습니다.
    
    Args:
        k_range: K 값 범위
        sse_list: 각 K에 대한 SSE 값 리스트
        min_drop_ratio: 초기 SSE 대비 감소율이 이 값 이하면 "완만"으로 간주
        
    Returns:
        int or None: 찾은 elbow point K 값 또는 None
    """
    if len(sse_list) < 3:
        return None
        
    initial_sse = sse_list[0]
    prev_drop_ratio = float('inf')
    
    for i in range(2, len(k_range)):
        curr_sse = sse_list[i]
        prev_sse = sse_list[i-1]
        drop = prev_sse - curr_sse
        
        # 현재 감소율
        curr_drop_ratio = drop / initial_sse
        
        # 이전 감소율과 비교해서 급격히 감소했다면 아직 elbow가 아님
        if curr_drop_ratio < min_drop_ratio and curr_drop_ratio < prev_drop_ratio * 0.7:
            return k_range[i]
            
        prev_drop_ratio = curr_drop_ratio
        
    return None

def _get_elbow_point(k_range, sse_list):
    """
    SSE의 수렴 지점을 더 정확하게 찾기 위한 개선된 elbow point 탐지
    """
    if len(k_range) < 3:
        return None

    try:
        # 1. 연속된 SSE 간의 감소율 계산
        decreases = []
        for i in range(1, len(sse_list)):
            decrease = (sse_list[i-1] - sse_list[i]) / sse_list[i-1]
        decreases.append(decrease)
        
        # 2. 감소율의 감소율 계산 (2차 미분 개념)
        decrease_changes = []
        for i in range(1, len(decreases)):
            change = abs(decreases[i-1] - decreases[i])
            decrease_changes.append(change)
        
        # 3. 수렴 판단을 위한 임계값 설정
        # - 감소율이 충분히 작고 (DECREASE_THRESHOLD)
        # - 감소율의 변화도 충분히 작은 (CHANGE_THRESHOLD) 지점
        DECREASE_THRESHOLD = 0.03  # 3% 미만의 감소율
        CHANGE_THRESHOLD = 0.01    # 감소율 변화가 1% 미만
        
        for i, (decrease, change) in enumerate(zip(decreases[1:], decrease_changes)):
            if decrease < DECREASE_THRESHOLD and change < CHANGE_THRESHOLD:
                # k_range의 인덱스 보정 (+2: decreases[1:] 때문에 1, change 계산때문에 1)
                return k_range[i + 2]
        
        # 4. 위 방법으로 못찾으면, 기존 KneeLocator 사용
        # - 단, 더 보수적인 파라미터 적용
        knee_finder = KneeLocator(
            x=k_range,
            y=sse_list,
            curve='convex',
            direction='decreasing',
            S=2.0,  # 더 보수적인 값 (기본값 1.0)
            interp_method='polynomial'  # 더 부드러운 곡선
        )
        
        if knee_finder.knee is not None:
            return knee_finder.knee
            
        # 5. 마지막 수단: 기하학적 방법
        # - 첫점과 끝점을 이은 선에서 가장 먼 점
        p1 = np.array([k_range[0], sse_list[0]])
        p2 = np.array([k_range[-1], sse_list[-1]])
        
        max_dist = -1
        elbow_k = None
        
        for k, sse in zip(k_range[1:-1], sse_list[1:-1]):
            p = np.array([k, sse])
            d = np.abs(np.cross(p2 - p1, p - p1)) / np.linalg.norm(p2 - p1)
            if d > max_dist:
                max_dist = d
                elbow_k = k
                
        return elbow_k

    except Exception as e:
        logger.warning(f"[_get_elbow_point] knee point 탐색 중 오류 발생: {str(e)}")
        return None

############################################
# 모델 평가
############################################
def create_clustering_model(model_type, n_clusters):
    if model_type=='kmeans':
        return KMeans(n_clusters=n_clusters, random_state=CONFIG['RANDOM_SEED'], n_init=15)
    elif model_type=='gmm':
        return GaussianMixture(n_components=n_clusters, n_init=5, random_state=CONFIG['RANDOM_SEED'])
    elif model_type=='hierarchical':
        return AgglomerativeClustering(n_clusters=n_clusters, linkage='ward')
    else:
        raise ValueError(f"Unknown model: {model_type}")

def get_labels_by_model(model, model_type, features):
    """
    모델 종류(kmeans/gmm/hierarchical)에 따라 라벨(클러스터 번호) 추출 방식을 달리함.
    """
    if model_type == 'kmeans' or model_type == 'hierarchical':
        # KMeans와 Hierarchical은 model.labels_로 라벨 획득
        return model.labels_
    elif model_type == 'gmm':
        # GaussianMixture는 model.labels_가 없으므로 predict 사용
        return model.predict(features)
    else:
        raise ValueError(f"Unsupported model_type: {model_type}")

def evaluate_models(features, n_clusters):
    results = {}
    
    def _fit_and_evaluate(mt):
        try:
            # KMeans의 경우 n_jobs 파라미터 추가
            if mt == 'kmeans':
                model = KMeans(
                    n_clusters=n_clusters, 
                    random_state=CONFIG['RANDOM_SEED'], 
                    n_init=15,
                )
            else:
                model = create_clustering_model(mt, n_clusters)
            
            labels = model.fit_predict(features)
            score = compute_composite_score(features, labels)
            return mt, {
                'model': model,
                'labels': labels,
                'scores': score
            }
        except Exception as e:
            logger.error(f"[evaluate_models] {mt} 실패: {str(e)}")
            return mt, None

    # 병렬로 모델 평가 실행
    model_results = Parallel(n_jobs=CONFIG['N_JOBS'], backend=CONFIG['PARALLEL_BACKEND'])(
        delayed(_fit_and_evaluate)(mt) for mt in ['kmeans','gmm','hierarchical']
    )
    
    # 결과 취합
    for mt, result in model_results:
        if result is not None:
            results[mt] = result
            
    return results


############################################
# 가중치 로드/저장
############################################
def resolve_path(file_path):
    p=Path(file_path)
    return p if p.is_absolute() else CONFIG['RESOURCE_DIR']/p

def load_weights(cluster_type):
    fpath = CONFIG['USER_WEIGHTS_FILE'] if cluster_type=='user' else CONFIG['COUPLE_WEIGHTS_FILE']
    full = resolve_path(fpath)
    
    # 파일 존재 여부 명시적 체크 및 로깅
    if not full.exists():
        logger.warning(f"[load_weights] {cluster_type} 가중치 파일이 존재하지 않음: {full}")
        return DEFAULT_WEIGHTS[cluster_type]
        
    try:
        with open(full, 'r', encoding='utf-8') as ff:
            data = json.load(ff)
            
        w = np.array(data['weights'])
        k = data['optimal_k']
        sc = data['score']
        logger.info(f"[load_weights] {cluster_type} 가중치 로드 성공: {full}")
        return {
            'weights': w,
            'optimal_k': k,
            'score': sc
        }
    except Exception as e:
        logger.error(f"[load_weights] {cluster_type} 가중치 파일 로드 실패: {str(e)}")
        return DEFAULT_WEIGHTS[cluster_type]

def save_weights(ow, cluster_type):
    fpath = CONFIG['USER_WEIGHTS_FILE'] if cluster_type=='user' else CONFIG['COUPLE_WEIGHTS_FILE']
    full = resolve_path(fpath)
    
    # numpy 배열을 list로 변환하고 float으로 타입 변환
    data = {
        'weights': [float(x) for x in ow['weights']],
        'optimal_k': int(ow['optimal_k']),
        'score': float(ow['score'])
    }
    
    try:
        with open(full, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        logger.info(f"[save_weights] {cluster_type} 가중치 저장 성공: {full}")
    except Exception as e:
        logger.error(f"[save_weights] {cluster_type} 가중치 저장 실패: {str(e)}")


############################################
# K 탐색
############################################
def find_optimal_k(features, k_range=range(2,29), use_elbow=True):
    """
    최적의 클러스터 수(K) 탐색
    
    1. Elbow Method: SSE 감소율 분석
    2. 복합 평가 지표 활용
    3. 병렬 처리로 성능 최적화
    
    Args:
        features: 특성 행렬
        k_range: 탐색할 K 범위
        use_elbow: Elbow Method 사용 여부
    
    Returns:
        tuple: (optimal_k, sse_list, elbow_k)
            - optimal_k: 최적 K
            - sse_list: 각 K별 SSE 값
            - elbow_k: 엘보우 포인트 K (없으면 None)
    """
    def _evaluate_k(k):
        km = KMeans(n_clusters=k, random_state=CONFIG['RANDOM_SEED'], n_init=15)
        labels = km.fit_predict(features)
        return (k, km.inertia_, compute_composite_score(features, labels)['composite'])

    # 병렬로 K 평가
    results = Parallel(n_jobs=CONFIG['N_JOBS'], backend=CONFIG['PARALLEL_BACKEND'])(
        delayed(_evaluate_k)(k) for k in k_range
    )
    
    # 결과 정렬 및 분리
    results.sort(key=lambda x: x[0])  # k 기준 정렬
    k_values = [r[0] for r in results]  # k 값들
    sse_list = [r[1] for r in results]  # SSE 값들
    composite_list = [r[2] for r in results]

    elbow_k = None
    threshold_k = None

    # elbow point 탐색
    if use_elbow and len(k_range) >= 3:
        elbow_candidate = _get_elbow_point(k_values, sse_list)
        if elbow_candidate is not None:
            elbow_k = elbow_candidate

    # threshold point 탐색
    threshold_candidate = custom_elbow_by_threshold(k_values, sse_list, min_drop_ratio=0.025)
    if threshold_candidate is not None:
        threshold_k = threshold_candidate

    # 결정 로직
    if elbow_k is not None:
        final_k = elbow_k
        logger.info(
            f"[find_optimal_k] elbow={elbow_k}, threshold={threshold_k}, "
            f"elbow 우선 선택 => 최종 K={final_k}"
        )
        return final_k, sse_list, elbow_k

    if threshold_k is not None:
        final_k = threshold_k
        logger.info(
            f"[find_optimal_k] elbow=None, threshold={threshold_k} => 최종 K={final_k}"
        )
        return final_k, sse_list, None

    best_k_idx = np.argmax(composite_list)
    final_k = k_values[best_k_idx]
    logger.info(f"[find_optimal_k] elbow/threshold 없음 → Composite Score 최대 K={final_k}")
    return final_k, sse_list, None


############################################
# 가중치 최적화
############################################
def optimize_user_weights(features_scaled, n_clusters):
    loaded=load_weights('user')
    if loaded and not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("[optimize_user_weights] 기존 사용")
        save_weights(loaded,'user')
        return loaded

    if not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("[optimize_user_weights] 기본 가중치")
        save_weights(DEFAULT_WEIGHTS['user'],'user')
        return DEFAULT_WEIGHTS['user']

    wr=CONFIG['USER_CLUSTERS']['WEIGHT_RANGES']
    space=[
        Real(wr['age'][0],wr['age'][1],name='age_weight'),
        Real(wr['budget'][0],wr['budget'][1],name='budget_weight'),
        Real(wr['mbti_ie'][0],wr['mbti_ie'][1],name='mbti_ie_weight'),
        Real(wr['mbti_sn'][0],wr['mbti_sn'][1],name='mbti_sn_weight'),
        Real(wr['mbti_tf'][0],wr['mbti_tf'][1],name='mbti_tf_weight'),
        Real(wr['mbti_jp'][0],wr['mbti_jp'][1],name='mbti_jp_weight'),
    ]

    @use_named_args(space)
    def objective(**params):
        w=np.array([
            params['age_weight'],
            params['budget_weight'],
            params['mbti_ie_weight'],
            params['mbti_sn_weight'],
            params['mbti_tf_weight'],
            params['mbti_jp_weight']
        ])
        wf=features_scaled*w
        r=evaluate_models(wf,n_clusters)
        best=max([val['scores']['composite'] for val in r.values()])
        return -best

    out=gp_minimize(
        objective,
        space,
        n_calls=CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts=CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state=CONFIG['RANDOM_SEED'],
        n_jobs=CONFIG['N_JOBS']
    )
    best={
        'weights': np.array(out.x),
        'optimal_k': n_clusters,
        'score': -out.fun
    }
    save_weights(best,'user')
    return best



def optimize_couple_weights(features_scaled, n_clusters):
    loaded=load_weights('couple')
    if loaded and not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("[optimize_couple_weights] 기존 사용")
        save_weights(loaded,'couple')
        return loaded

    if not CONFIG['OPTIMIZE_WEIGHTS']:
        logger.info("[optimize_couple_weights] 기본 가중치")
        save_weights(DEFAULT_WEIGHTS['couple'],'couple')
        return DEFAULT_WEIGHTS['couple']

    wr=CONFIG['COUPLE_CLUSTERS']['WEIGHT_RANGES']
    space=[
        Real(wr['male_age'][0],wr['male_age'][1],name='male_age_weight'),
        Real(wr['female_age'][0],wr['female_age'][1],name='female_age_weight'),
        Real(wr['budget'][0],wr['budget'][1],name='budget_weight'),
        Real(wr['marriage_month_sin'][0],wr['marriage_month_sin'][1],name='marriage_month_sin_weight'),
        Real(wr['marriage_month_cos'][0],wr['marriage_month_cos'][1],name='marriage_month_cos_weight'),
        Real(wr['marriage_day_sin'][0],wr['marriage_day_sin'][1],name='marriage_day_sin_weight'),
        Real(wr['marriage_day_cos'][0],wr['marriage_day_cos'][1],name='marriage_day_cos_weight'),
        Real(wr['age_diff'][0],wr['age_diff'][1],name='age_diff_weight'),
        Real(wr['user_cluster_distance'][0],wr['user_cluster_distance'][1],name='user_cluster_distance_weight')
    ]

    @use_named_args(space)
    def objective(**params):
        w=np.array([
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
        wf=features_scaled*w
        r=evaluate_models(wf,n_clusters)
        best=max([val['scores']['composite'] for val in r.values()])
        return -best

    out=gp_minimize(
        objective,
        space,
        n_calls=CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts=CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state=CONFIG['RANDOM_SEED'],
        n_jobs=CONFIG['N_JOBS']
    )
    best={
        'weights': np.array(out.x),
        'optimal_k': n_clusters,
        'score': -out.fun
    }
    save_weights(best,'couple')
    return best

############################################
# 결과 할당용
############################################
def assign_cluster_labels(user_ids, user_labels, couple_ids, couple_labels):
    """
    별도로 레이블을 매핑에서 활용할 수 있도록 준비.
    """
    user_assignments = dict(zip(user_ids, user_labels))
    couple_assignments = dict(zip(couple_ids, couple_labels))
    return user_assignments, couple_assignments

############################################
# (추가) 스케일러 파라미터 저장 함수
############################################
def save_scaler_params(scaler_dict: dict, cluster_type: str):
    """
    스케일러 파라미터를 JSON으로 저장.
    scaler_dict: {"male_age": StandardScaler(), "budget": MinMaxScaler(), ...} 형태
    cluster_type: "user" 또는 "couple"
    """
    output_file = CONFIG['RESOURCE_DIR'] / f"{cluster_type}_scaler_params.json"
    result = {}
    for feature_name, scaler in scaler_dict.items():
        if scaler is None:
            # 스케일링이 필요 없는 MBTI 등
            result[feature_name] = None
            continue

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

        result[feature_name] = params

    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(result, f, indent=2, ensure_ascii=False)
    logger.info(f"[save_scaler_params] {cluster_type}_scaler_params.json 저장 => {output_file}")

############################################
# (추가) 클러스터 중심점 추출
############################################
def get_cluster_centers(model, model_type: str, features: np.ndarray):
    """
    model_type에 따라 클러스터 중심(센터) 좌표를 가져온다.
    - kmeans: model.cluster_centers_
    - gmm: model.means_
    - hierarchical: 라벨별 평균좌표
    """
    if model_type == 'kmeans':
        return model.cluster_centers_
    elif model_type == 'gmm':
        # GMM은 means_가 클러스터별 평균 벡터
        return model.means_
    elif model_type == 'hierarchical':
        # Hierarchical은 cluster_centers_가 없으므로 라벨별 평균 계산
        labels = model.labels_
        centers = []
        for c in np.unique(labels):
            idx = np.where(labels==c)[0]
            centers.append(features[idx].mean(axis=0))
        return np.array(centers)
    else:
        logger.warning(f"[get_cluster_centers] Unknown model_type={model_type}")
        return None

def save_cluster_centers(cluster_type: str, model_info: dict, feature_names: list, weighted_features: np.ndarray):
    """
    model_info: {"model":..., "scores":..., "labels":...} 형태 
                (evaluate_models 등의 결과)
    """
    model = model_info['model']
    # 어떤 모델인지 판단 (kmeans/gmm/hierarchical)
    # 사용자가 따로 "model_type"을 저장해두지 않았다면, 여기서 키워드로 구분해도 됨
    model_type = None
    if isinstance(model, KMeans):
        model_type = 'kmeans'
    elif isinstance(model, GaussianMixture):
        model_type = 'gmm'
    elif isinstance(model, AgglomerativeClustering):
        model_type = 'hierarchical'
    else:
        logger.warning("[save_cluster_centers] 지원하지 않는 모델 타입")
        return

    centers = get_cluster_centers(model, model_type, weighted_features)
    if centers is None:
        logger.warning("[save_cluster_centers] 클러스터 중심 계산 불가")
        return

    cluster_centers_dict = {}
    for idx, center_vec in enumerate(centers):
        one_center = {}
        for fname, val in zip(feature_names, center_vec):
            one_center[fname] = float(val)
        cluster_centers_dict[f"cluster_{idx}"] = one_center

    output_file = CONFIG['RESOURCE_DIR'] / f"{cluster_type}_cluster_centers.json"
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(cluster_centers_dict, f, indent=2, ensure_ascii=False)
    logger.info(f"[save_cluster_centers] {cluster_type}_cluster_centers.json 저장 => {output_file}")

############################################
# (추가) 클러스터 할당결과 JSON 저장
############################################
def save_assignments(cluster_type: str,
                     ids: np.ndarray,
                     labels: np.ndarray,
                     weighted_features: np.ndarray,
                     id_name: str):
    """
    cluster_type: "user" 또는 "couple"
    ids: 예) user_ids or couple_ids
    labels: 클러스터 레이블
    weighted_features: 실제 가중치 적용된 특성값
    id_name: "userid" or "coupleid" 등 JSON에 넣을 키
    """
    assignments = []
    for i, item_id in enumerate(ids):
        row = {
            id_name: int(item_id),
            "cluster": int(labels[i]),
            "weighted_features": weighted_features[i].tolist()
        }
        assignments.append(row)

    output_file = CONFIG['RESOURCE_DIR'] / f"{cluster_type}_assignments.json"
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(assignments, f, indent=2, ensure_ascii=False)
    logger.info(f"[save_assignments] {cluster_type}_assignments.json 저장 => {output_file}")

############################################
# JSON 파일 로드
############################################
@measure_time
def load_input_json():
    input_file = CONFIG['RESOURCE_DIR'] / 'input_data.json'
    logger.info(f"[load_input_json] 시도: {input_file}")
    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)

        if isinstance(data, list):
            data = {"couples": data}

        logger.info(f"[load_input_json] 데이터 로드 성공: {len(data.get('couples', [])):,} 커플")
        
        # 데이터 유효성 검사
        if 'couples' not in data:
            raise ValueError("JSON 데이터에 'couples' 키가 없습니다")
            
        for couple in data['couples']:
            if not all(k in couple for k in ['couple_id', 'female', 'male', 'budget', 'marriage_date']):
                raise ValueError("필수 필드가 누락된 커플 데이터가 있습니다")
            if not all(k in couple['female'] for k in ['user_id', 'age', 'mbti']):
                raise ValueError("필수 필드가 누락된 여성 데이터가 있습니다")
            if not all(k in couple['male'] for k in ['user_id', 'age', 'mbti']):
                raise ValueError("필수 필드가 누락된 남성 데이터가 있습니다")
                
        return data
        
    except FileNotFoundError as fnf:
        logger.exception(f"[load_input_json] 파일이 없습니다: {input_file}")
        raise  # 상위에서 잡아서 sys.exit(1) 처리
    except Exception as e:
        logger.exception(f"[load_input_json] 예기치 않은 에러 발생")
        raise

############################################
# (추가) 최종 요약 파일 생성 함수
############################################
def save_final_summary(result):
    """
    클러스터링 결과 요약 저장
    
    Args:
        result (dict): {
            "user_results": {
                "best_model_type": str,
                "scores": dict,
                "optimal_k": int,
                ...
            },
            "couple_results": {
                "best_model_type": str,
                "scores": dict,
                "optimal_k": int,
                ...
            }
        }
    
    Saves:
        clustering_summary.json:
            - 모델 유형
            - 평가 점수
            - 최적 K값
            - 타임스탬프
    """
    summary_file = CONFIG['RESOURCE_DIR'] / 'clustering_summary.json'
    
    # user_scores, couple_scores에 np.float64 타입이 있을 수 있으므로 float() 변환
    user_scores_fixed = {}
    for k, v in result["user_results"]["scores"].items():
        user_scores_fixed[k] = float(v)
    
    couple_scores_fixed = {}
    for k, v in result["couple_results"]["scores"].items():
        couple_scores_fixed[k] = float(v)
    
    summary_data = {
        "user_best_model": str(result["user_results"]["best_model_type"]),
        "user_scores": user_scores_fixed,
        "user_optimal_k": int(result["user_results"]["optimal_k"]),
        "couple_best_model": str(result["couple_results"]["best_model_type"]),
        "couple_scores": couple_scores_fixed,
        "couple_optimal_k": int(result["couple_results"]["optimal_k"]),
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S")
    }
    
    with open(summary_file, 'w', encoding='utf-8') as f:
        json.dump(summary_data, f, indent=2, ensure_ascii=False)
    
    logger.info(f"[save_final_summary] 요약 파일 저장 => {summary_file}")

############################################
# (추가) 3D 시각화 함수
############################################
def plot_3d_clusters(features, labels, cluster_centers, title, output_path, 
                    feature_names=None, compress=None):
    """
    클러스터링 결과를 3D 산점도로 시각화
    
    Args:
        features: 특성 행렬
        labels: 클러스터 레이블
        cluster_centers: 클러스터 중심점
        title: 그래프 제목
        output_path: 저장 경로
        feature_names: 축 이름 (기본값: None)
        compress: PCA 압축 사용 여부 (기본값: CONFIG 설정값)
    """
    if not CONFIG['VISUALIZATION']['ENABLED']:
        logger.info("[plot_3d_clusters] 시각화 비활성화 상태")
        return

    # CONFIG에서 설정 가져오기
    viz_config = CONFIG['VISUALIZATION']
    compress = viz_config['USE_PCA'] if compress is None else compress

    # 1. 데이터 준비 - PCA 압축 적용
    pca = PCA(n_components=3)
    features_3d = pca.fit_transform(features)
    centers_3d = pca.transform(cluster_centers)
    
    # 2. 그래프 설정
    plt.figure(figsize=viz_config['FIGURE_SIZE'])
    ax = plt.axes(projection='3d')
    
    # 3. 클러스터별 색상 설정 - 더 다양한 색상 팔레트 사용
    n_clusters = len(np.unique(labels))
    colors = plt.cm.tab20(np.linspace(0, 1, n_clusters))  # tab20 컬러맵 사용
    
    # 4. 데이터 포인트 그리기 - 투명도와 크기 조정
    for i, color in enumerate(colors):
        mask = labels == i
        ax.scatter(
            features_3d[mask, 0],
            features_3d[mask, 1],
            features_3d[mask, 2],
            c=[color],
            label=f'Cluster {i}',
            alpha=0.7,  # 투명도 증가
            s=30  # 포인트 크기 감소
        )
    
    # 5. 클러스터 중심점 그리기 - 더 눈에 띄게
    ax.scatter(
        centers_3d[:, 0],
        centers_3d[:, 1],
        centers_3d[:, 2],
        c='red',
        marker='*',
        s=200,  # 중심점 크기 증가
        label='Centroids',
        edgecolor='black',  # 테두리 추가
        linewidth=1
    )
    
    # 6. 그래프 꾸미기
    explained_var = pca.explained_variance_ratio_ * 100
    ax.set_xlabel(f'PC1 ({explained_var[0]:.1f}%)')
    ax.set_ylabel(f'PC2 ({explained_var[1]:.1f}%)')
    ax.set_zlabel(f'PC3 ({explained_var[2]:.1f}%)')
    
    # 제목에 설명분산비 추가
    total_var = sum(explained_var)
    plt.title(f'{title}\nTotal Explained Variance: {total_var:.1f}%')
    
    # 범례 위치 조정 및 그리드 추가
    ax.legend(bbox_to_anchor=(1.15, 1), loc='upper left')
    ax.grid(True, alpha=0.3)
    
    # 시점 조정
    ax.view_init(elev=20, azim=45)
    
    # 7. 저장
    plt.tight_layout()
    plt.savefig(output_path, dpi=viz_config['DPI'], bbox_inches='tight')
    plt.close()
    
    logger.info(f"[plot_3d_clusters] 3D 클러스터링 시각화 저장 완료: {output_path}")
    logger.info(f"설명분산비: PC1={explained_var[0]:.1f}%, PC2={explained_var[1]:.1f}%, PC3={explained_var[2]:.1f}%")

############################################
# 메인
############################################
@measure_time
def main(config=CONFIG):
    """
    클러스터링 메인 실행 함수
    
    프로세스:
    1. 데이터 로드
    2. 사용자 클러스터링
        - 특성 추출 및 스케일링
        - 최적 K 탐색
        - 모델 선택 및 학습
    3. 커플 클러스터링
        - 특성 추출 및 스케일링
        - 사용자 클러스터 거리 계산
        - 최적 K 탐색
        - 모델 선택 및 학습
    4. 결과 저장
        - 클러스터 중심점
        - 할당 결과
        - 스케일러 파라미터
        - 요약 리포트
    
    Args:
        config (dict): 설정 파라미터
    
    Returns:
        dict: 클러스터링 결과 및 평가 지표
    """
    logger.info("=== 클러스터링 시작 ===")

    try:
        # 1. 데이터 로드
        if config['USE_DUMMY_DATA']:
            couples_json = create_dummy_couples_json(n_couples=config['SAMPLE_SIZE'])
            logger.info(f"[main] 더미데이터 생성 완료. 총 {len(couples_json['couples'])} 커플")
        else:
            couples_json = load_input_json()
            logger.info(f"[main] 실제 데이터 로드 완료. 총 {len(couples_json['couples'])} 커플")

        # 데이터 샘플 로깅 (최대 3커플 예시만)
        for sample_couple in couples_json['couples'][:3]:
            logger.info(f"  예시 커플: {sample_couple}")

        # 2. 사용자/커플 NumPy 변환
        user_arr, user_ids = users_to_numpy(couples_json)
        couple_arr, couple_ids, female_ids, male_ids = couples_to_numpy(couples_json)

        # 3. 사용자 전처리 (스케일링) & 최적 가중치
        user_arr_scaled, user_scalers = scale_user_features(user_arr)

        # -- 만약 K 탐색을 스킵한다면, config에서 지정된 기본 N_CLUSTERS 사용
        if config.get('SKIP_OPTIMIZE_K', False):
            user_k = config['USER_CLUSTERS']['N_CLUSTERS']
            user_sse_list = []
            user_elbow_k = None
            logger.info(f"[main] 사용자 K 탐색 스킵: user_k={user_k}")
        else:
            user_k_range = config.get('USER_K_RANGE', range(2,29))
            user_k, user_sse_list, user_elbow_k = find_optimal_k(
                features=user_arr_scaled, 
                k_range=user_k_range,  
                use_elbow=True
            )

        # 4. 사용자 가중치 최적화
        user_weights = optimize_user_weights(user_arr_scaled, user_k)
        weighted_user_features = user_arr_scaled * user_weights['weights']

        user_models = evaluate_models(weighted_user_features, user_weights['optimal_k'])
        best_user_name, best_user_data = max(user_models.items(), key=lambda x: x[1]['scores']['composite'])
        user_labels = get_labels_by_model(
            user_models[best_user_name]['model'], 
            best_user_name, 
            user_arr_scaled
        )
        logger.info(f"[사용자] 선택 모델={best_user_name}, 점수={best_user_data['scores']}")

        save_scaler_params(user_scalers, "user")

        # * PLOT_ELBOW_GRAPHS=true & SKIP_OPTIMIZE_K=false 인 경우에만 엘보우 그래프 생성
        if config.get('PLOT_ELBOW_GRAPHS', True) and not config.get('SKIP_OPTIMIZE_K', False):
            plot_elbow_graph(user_k_range, user_sse_list, user_elbow_k, "user")

        # 5. 유저 간 거리 -> 커플 user_cluster_distance
        user_cluster_distance = []
        user_id_to_index = { uid: i for i, uid in enumerate(user_ids) }
        for i in range(len(couple_arr)):
            fid = female_ids[i]
            mid = male_ids[i]
            if fid in user_id_to_index and mid in user_id_to_index:
                fvec = weighted_user_features[user_id_to_index[fid]]
                mvec = weighted_user_features[user_id_to_index[mid]]
                dist = np.linalg.norm(fvec - mvec)
            else:
                dist = 0.0
            user_cluster_distance.append(dist)
        user_cluster_distance = np.array(user_cluster_distance)

        # 커플 배열 마지막 열(8)에 갱신
        couple_arr[:, 8] = user_cluster_distance

        # 6. 커플 전처리/스케일링
        couple_arr_scaled, couple_scalers = scale_couple_features(couple_arr)

        # -- K 탐색 스킵 여부
        if config.get('SKIP_OPTIMIZE_K', False):
            couple_k = config['COUPLE_CLUSTERS']['N_CLUSTERS']
            couple_sse_list = []
            couple_elbow_k = None
            logger.info(f"[main] 커플 K 탐색 스킵: couple_k={couple_k}")
        else:
            couple_k_range = config.get('COUPLE_K_RANGE', range(2, 21))
            couple_k, couple_sse_list, couple_elbow_k = find_optimal_k(
                couple_arr_scaled, 
                k_range=couple_k_range, 
                use_elbow=True
            )

        # 7. 커플 가중치 최적화
        couple_weights = optimize_couple_weights(couple_arr_scaled, couple_k)
        weighted_couple_features = couple_arr_scaled * couple_weights['weights']

        couple_models = evaluate_models(weighted_couple_features, couple_weights['optimal_k'])
        best_couple_name, best_couple_data = max(couple_models.items(), key=lambda x: x[1]['scores']['composite'])
        couple_labels = get_labels_by_model(
            couple_models[best_couple_name]['model'], 
            best_couple_name, 
            couple_arr_scaled
        )
        logger.info(f"[커플] 선택 모델={best_couple_name}, 점수={best_couple_data['scores']}")

        save_scaler_params(couple_scalers, "couple")

        # * PLOT_ELBOW_GRAPHS=true & SKIP_OPTIMIZE_K=false 인 경우에만 엘보우 그래프 생성
        if config.get('PLOT_ELBOW_GRAPHS', True) and not config.get('SKIP_OPTIMIZE_K', False):
            plot_elbow_graph(couple_k_range, couple_sse_list, couple_elbow_k, "couple")

        # 8. 레이블 매핑
        user_assign, couple_assign = assign_cluster_labels(user_ids, user_labels, couple_ids, couple_labels)

        # 9. 클러스터 중심점 저장
        save_cluster_centers("user", user_models[best_user_name], config['USER_CLUSTERS']['FEATURES'], weighted_user_features)
        save_cluster_centers("couple", couple_models[best_couple_name], config['COUPLE_CLUSTERS']['FEATURES'], weighted_couple_features)

        # 10. 할당결과 저장
        save_assignments("user", user_ids, user_labels, weighted_user_features, id_name="userid")
        save_assignments("couple", couple_ids, couple_labels, weighted_couple_features, id_name="coupleid")

        # 11. 3D 시각화
        if CONFIG['VISUALIZATION']['ENABLED']:
            # 사용자 클러스터 시각화
            user_centers = get_cluster_centers(
                user_models[best_user_name]['model'],
                best_user_name,
                weighted_user_features
            )
            plot_3d_clusters(
                weighted_user_features,
                user_labels,
                user_centers,
                CONFIG['VISUALIZATION']['USER_FEATURES']['title'],
                CONFIG['RESOURCE_DIR'] / CONFIG['VISUALIZATION']['USER_FEATURES']['filename'],
                feature_names=CONFIG['USER_CLUSTERS']['FEATURES']
            )
            
            # 커플 클러스터 시각화
            couple_centers = get_cluster_centers(
                couple_models[best_couple_name]['model'],
                best_couple_name,
                weighted_couple_features
            )
            plot_3d_clusters(
                weighted_couple_features,
                couple_labels,
                couple_centers,
                CONFIG['VISUALIZATION']['COUPLE_FEATURES']['title'],
                CONFIG['RESOURCE_DIR'] / CONFIG['VISUALIZATION']['COUPLE_FEATURES']['filename'],
                feature_names=CONFIG['COUPLE_CLUSTERS']['FEATURES']
            )

        logger.info("=== 클러스터링 완료 ===")

        result = {
            "user_results": {
                "best_model_type": best_user_name,
                "scores": best_user_data['scores'],
                "labels": user_labels.tolist(),
                "weights": user_weights,
                "optimal_k": user_k
            },
            "couple_results": {
                "best_model_type": best_couple_name,
                "scores": best_couple_data['scores'],
                "labels": couple_labels.tolist(),
                "weights": couple_weights,
                "optimal_k": couple_k
            },
            "assignments": {
                "user": user_assign,
                "couple": couple_assign
            }
        }

        save_final_summary(result)
        return result

    except Exception as e:
        logger.exception(f"[main] 예외 발생: {e}")
        raise

############################################
# 실행부
############################################
if __name__=="__main__":
    if len(sys.argv) > 1:
        CONFIG['USE_DUMMY_DATA'] = (sys.argv[1].lower() == 'true')
        
    os.makedirs(CONFIG['RESOURCE_DIR'], exist_ok=True)
    
    try:
        result = main(CONFIG)
        logger.info("프로그램이 성공적으로 완료되었습니다.")
        sys.exit(0)
    except FileNotFoundError as fe:
        logger.exception(f"[__main__] 파일을 찾을 수 없습니다: {fe}")
        sys.exit(1)
    except Exception as e:
        logger.exception(f"[__main__] 알 수 없는 에러 발생: {e}")
        sys.exit(1)