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
from sklearn.decomposition import PCA
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.colors as mcolors
import seaborn as sns


# (Optional) t-SNE를 쓰려면 아래 활성화
# from sklearn.manifold import TSNE


################################################################################
# 설정 CONFIG
################################################################################
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_RESOURCE_DIR = SCRIPT_DIR.parent.parent.parent.parent / 'data' / 'resources'

CONFIG = {
    'USE_DUMMY_DATA': False,            # True 시 더미데이터 사용 / False 시 input_data.json 사용
    'SAMPLE_SIZE': 10000,               # 더미 커플 생성 시 개수
    'RESOURCE_DIR': Path(os.getenv('RESOURCE_DIR', str(DEFAULT_RESOURCE_DIR))),
    'RANDOM_SEED': 42,

    # K 탐색 범위 (2~30)
    'USER_K_RANGE': range(2, 31),
    'COUPLE_K_RANGE': range(2, 31),

    # 사용자 클러스터링 설정
    'USER_CLUSTERS': {
        'FEATURES': [
            'age','budget','mbti_ie','mbti_sn','mbti_tf','mbti_jp'
        ],
        # K 최적화를 건너뛸 때 사용할 기본 K
        'N_CLUSTERS': 9,
        'WEIGHT_RANGES': {
            # age_weight ∈ [2.0, 10.0]
            'age': (2.0, 10.0),
            # budget_weight ∈ [4.0, 16.0]
            'budget': (4.0, 16.0),
            # mbti_*_weight ∈ [0.5, 8.0]
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
        'N_CLUSTERS': 9,
        'WEIGHT_RANGES': {
            # male_age_weight, female_age_weight ∈ [2.0, 8.0]
            'male_age': (2.0, 8.0),
            'female_age': (2.0, 8.0),
            # budget_weight ∈ [4.0, 16.0]
            'budget': (4.0, 16.0),
            # date_weights ∈ [0.1, 5.0] (여기서는 0.5~5.0 정도)
            'marriage_month_sin': (0.5, 5.0),
            'marriage_month_cos': (0.5, 5.0),
            'marriage_day_sin': (0.1, 1.0),
            'marriage_day_cos': (0.1, 1.0),
            # age_diff_weight ∈ [0.5, 12.0]
            'age_diff': (0.5, 12.0),
            # distance_weight ∈ [0.5, 6.0]
            'user_cluster_distance': (0.5, 6.0)
        }
    },

    # 가중치 최적화 (Bayesian Optimization) 옵션
    'OPTIMIZE_WEIGHTS': False,
    'OPTIMIZE_N_CALLS': 100,            # 최대 반복 횟수
    'OPTIMIZATION_N_RANDOM_STARTS': 35, # 초기 탐색 횟수

    # Elbow Method 그래프 그릴지 여부
    'PLOT_ELBOW_GRAPHS': False,

    # K 탐색(최적 K 찾기) 스킵 (True 시 고정 K 사용)
    'SKIP_OPTIMIZE_K': True,

    # 결과 파일명
    'USER_WEIGHTS_FILE': 'user_optimized_weights.json',
    'COUPLE_WEIGHTS_FILE': 'couple_optimized_weights.json',

    # 병렬처리 설정
    'N_JOBS': max(1, multiprocessing.cpu_count() - 1),
    'PARALLEL_BACKEND': 'loky',

    # 3D 시각화 관련
    'VISUALIZATION': {
        'ENABLED': False,
        'FIGURE_SIZE': (12, 8),
        'DPI': 300,
        'POINT_SIZE': 50,
        'CENTROID_SIZE': 200,
        'ALPHA': 0.6,
        'REDUCTION_METHOD': 'pca',  # 'pca' 또는 'tsne'
        'USER_FEATURES': {
            'title': 'User Clusters',
            'filename': 'user_clusters_3d.png'
        },
        'COUPLE_FEATURES': {
            'title': 'Couple Clusters',
            'filename': 'couple_clusters_3d.png'
        }
    },
}

# 디폴트 가중치(파일이 존재하지 않을 경우 사용)
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


################################################################################
# 로깅 설정
################################################################################
def setup_logging():
    """파일로 로그 + 콘솔 로그 동시 출력 설정"""
    log_dir = CONFIG['RESOURCE_DIR'] / 'logs'
    log_dir.mkdir(parents=True, exist_ok=True)
    
    log_file = log_dir / f'clustering_{time.strftime("%Y%m%d_%H%M%S")}.log'
    
    file_handler = logging.FileHandler(log_file, encoding='utf-8')
    file_handler.setLevel(logging.INFO)
    file_handler.setFormatter(logging.Formatter(
        '%(asctime)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    ))
    
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_handler.setFormatter(logging.Formatter(
        '%(asctime)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    ))
    
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)  # 필요 시 INFO로 조정
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    return logger

logger = setup_logging()


################################################################################
# 시간 측정 데코레이터
################################################################################
def measure_time(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = time.time()
        res = func(*args, **kwargs)
        elapsed = time.time() - start_time
        logger.info(f"[TIME] {func.__name__} 완료 (소요: {elapsed:.2f}초)")
        return res
    return wrapper


################################################################################
# (Optional) 더미 데이터 생성
################################################################################
def create_dummy_couples_json(n_couples=2000):
    """
    테스트용 더미 커플 데이터를 생성합니다.
    (실제로는 input_data.json을 사용하도록 권장)
    """
    np.random.seed(CONFIG['RANDOM_SEED'])
    couples_data = {"couples": []}

    mbti_types = [
        'ISTJ','ISFJ','INFJ','INTJ','ISTP','ISFP','INFP','INTP',
        'ESTP','ESFP','ENFP','ENTP','ESTJ','ESFJ','ENFJ','ENTJ'
    ]
    mbti_weights = np.ones(len(mbti_types)) / len(mbti_types)  # 단순 균등

    for i in range(n_couples):
        female_age = np.random.randint(23, 40)
        male_age = np.random.randint(female_age, 45)
        budget = np.random.randint(2000, 12000) * 10000

        f_mbti = np.random.choice(mbti_types, p=mbti_weights)
        m_mbti = np.random.choice(mbti_types, p=mbti_weights)
        month = np.random.randint(1, 13)
        day = np.random.randint(1, 29)

        couple_dict = {
            "couple_id": i+1,
            "female": {
                "user_id": (i+1)*2 -1,
                "age": female_age,
                "mbti": f_mbti
            },
            "male": {
                "user_id": (i+1)*2,
                "age": male_age,
                "mbti": m_mbti
            },
            "budget": budget,
            "marriage_date": f"{month:02d}-{day:02d}"
        }
        couples_data["couples"].append(couple_dict)

    return couples_data


################################################################################
# MBTI / 날짜 인코딩
################################################################################
def mbti_to_encoding(mbti_str):
    """
    MBTI 문자열 -> 4차원 이진 인코딩
     - 첫 글자: I/E => 0/1
     - 둘째 글자: S/N => 0/1
     - 셋째 글자: T/F => 0/1
     - 넷째 글자: J/P => 0/1
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
    결혼 날짜(월-일)를 sin/cos 방식으로 순환 인코딩
    - month: [1..12], day: [1..31]
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


################################################################################
# JSON -> NumPy 변환
################################################################################
def users_to_numpy(couples_json):
    """
    커플 JSON에서 사용자별 특성(나이, 예산, MBTI)을 추출하여 NumPy 배열로 변환
      shape=(2N, 6): [age, budget, mbti_ie, mbti_sn, mbti_tf, mbti_jp]
    """
    n_couples = len(couples_json['couples'])
    user_feat = np.zeros((n_couples*2, 6), dtype=float)
    user_ids  = np.zeros(n_couples*2, dtype=int)

    for i, c in enumerate(couples_json['couples']):
        f_mbti = mbti_to_encoding(c['female']['mbti'])
        user_feat[i*2] = [
            c['female']['age'],
            c['budget'],
            f_mbti[0], f_mbti[1], f_mbti[2], f_mbti[3]
        ]
        user_ids[i*2] = c['female']['user_id']

        m_mbti = mbti_to_encoding(c['male']['mbti'])
        user_feat[i*2+1] = [
            c['male']['age'],
            c['budget'],
            m_mbti[0], m_mbti[1], m_mbti[2], m_mbti[3]
        ]
        user_ids[i*2+1] = c['male']['user_id']

    return user_feat, user_ids

def couples_to_numpy(couples_json):
    """
    커플 JSON에서 커플 특성(남/여 나이, 예산, 결혼일 sin/cos, 나이차, 추후 계산되는 유저클러스터거리)을 NumPy로
      shape=(N,9)
        - [male_age, female_age, budget, msin, mcos, dsin, dcos, age_diff, user_dist]
    """
    n_couples = len(couples_json['couples'])
    couple_feat = np.zeros((n_couples, 9), dtype=float)
    couple_ids  = np.zeros(n_couples, dtype=int)
    female_ids  = np.zeros(n_couples, dtype=int)
    male_ids    = np.zeros(n_couples, dtype=int)

    for i, c in enumerate(couples_json['couples']):
        couple_ids[i] = c['couple_id']
        female_ids[i] = c['female']['user_id']
        male_ids[i]   = c['male']['user_id']

        msin, mcos, dsin, dcos = encode_cyclic_date(c['marriage_date'])
        age_diff = c['male']['age'] - c['female']['age']

        couple_feat[i] = [
            c['male']['age'],
            c['female']['age'],
            c['budget'],
            msin, mcos,
            dsin, dcos,
            age_diff,
            0.0  # user_cluster_distance(추후 계산)
        ]

    return couple_feat, couple_ids, female_ids, male_ids


################################################################################
# 스케일링
################################################################################
def scale_user_features(user_array):
    """
    사용자 특성 스케일링
      - 나이: StandardScaler
      - 예산: MinMaxScaler
      - MBTI: 스케일 없이 그대로
    """
    scaled = user_array.copy()
    age_scaler = StandardScaler()
    budget_scaler = MinMaxScaler()

    scaled[:,0:1] = age_scaler.fit_transform(scaled[:,0:1])   # 나이
    scaled[:,1:2] = budget_scaler.fit_transform(scaled[:,1:2])# 예산

    scalers = {
        'age': age_scaler,
        'budget': budget_scaler,
        'mbti_ie': None,
        'mbti_sn': None,
        'mbti_tf': None,
        'mbti_jp': None
    }
    return scaled, scalers

def scale_couple_features(couple_array):
    """
    커플 특성 스케일링
      - (남/여 나이: StandardScaler)
      - 예산: MinMaxScaler
      - 날짜 sin/cos: MinMaxScaler
      - 나이차: MinMaxScaler
      - 유저클러스터거리: MinMaxScaler
    """
    scaled = couple_array.copy()
    male_scaler   = StandardScaler()
    female_scaler = StandardScaler()
    budget_scaler = MinMaxScaler()
    msin_scaler   = MinMaxScaler()
    mcos_scaler   = MinMaxScaler()
    dsin_scaler   = MinMaxScaler()
    dcos_scaler   = MinMaxScaler()
    agediff_scaler= MinMaxScaler()
    userdist_scaler = MinMaxScaler()

    scaled[:,0:1] = male_scaler.fit_transform(scaled[:,0:1])
    scaled[:,1:2] = female_scaler.fit_transform(scaled[:,1:2])
    scaled[:,2:3] = budget_scaler.fit_transform(scaled[:,2:3])
    scaled[:,3:4] = msin_scaler.fit_transform(scaled[:,3:4])
    scaled[:,4:5] = mcos_scaler.fit_transform(scaled[:,4:5])
    scaled[:,5:6] = dsin_scaler.fit_transform(scaled[:,5:6])
    scaled[:,6:7] = dcos_scaler.fit_transform(scaled[:,6:7])
    scaled[:,7:8] = agediff_scaler.fit_transform(scaled[:,7:8])
    scaled[:,8:9] = userdist_scaler.fit_transform(scaled[:,8:9])

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


################################################################################
# 평가 지표 (Silhouette 등)
################################################################################
def compute_dunn_index(features, labels):
    """
    Dunn 지수 계산: (클러스터 간 최소거리) / (클러스터 내 최대반경)
    값이 높을수록 좋음
    """
    labs = np.unique(labels)
    if len(labs) < 2:
        return 0

    clusters = [np.where(labels==lb)[0] for lb in labs]
    if len(clusters) < 2:
        return 0

    min_inter = float('inf')
    max_intra = 0

    for i, idx_i in enumerate(clusters):
        pts_i = features[idx_i]
        if len(pts_i) > 1:
            dist_i = pdist(pts_i).max()
            max_intra = max(max_intra, dist_i)

        for j in range(i+1, len(clusters)):
            pts_j = features[clusters[j]]
            dist_ij = cdist(pts_i, pts_j).min()
            min_inter = min(min_inter, dist_ij)

    if max_intra == 0:
        return 0
    return float(min_inter / max_intra)

def compute_composite_score(features, labels):
    """
    복합 점수 = 0.4*실루엣 + 0.2*(1/(db+1)) + 0.2*(ch/적당클립) + 0.2*Dunn지수
    """
    s = silhouette_score(features, labels)
    db= davies_bouldin_score(features, labels)
    ch= calinski_harabasz_score(features, labels)
    dn= compute_dunn_index(features, labels)

    # 범위 정규화
    s_norm = (s + 1)/2.0
    db_norm= 1.0/(db+1)
    ch_norm= min(ch/10000, 1.0)
    dn_norm= min(dn, 1.0)

    composite = 0.4*s_norm + 0.2*db_norm + 0.2*ch_norm + 0.2*dn_norm
    return {
        'composite': composite,
        'silhouette': s,
        'davies_bouldin': db,
        'calinski_harabasz': ch,
        'dunn_index': dn
    }


################################################################################
# Elbow Method 관련
################################################################################
def plot_elbow_graph(k_range, sse_list, elbow_k, cluster_type):
    """
    SSE vs K 그래프 (Elbow Plot) 그리기
    """
    sns.set_style("whitegrid")
    plt.figure(figsize=(8,5), dpi=120)
    plt.plot(k_range, sse_list, marker='o', color='steelblue',
             linewidth=2.5, markersize=8, label='SSE')

    if elbow_k is not None and elbow_k in k_range:
        ek_sse = sse_list[k_range.index(elbow_k)]
        plt.plot(elbow_k, ek_sse, marker='o', color='red',
                 markersize=10, label=f'Knee={elbow_k}')

    plt.title(f'Elbow Plot ({cluster_type})\nSSE vs Number of Clusters', fontsize=14, fontweight='bold')
    plt.xlabel('Number of Clusters (k)', fontsize=12)
    plt.ylabel('SSE (Sum of Squared Errors)', fontsize=12)

    plt.xticks(k_range)
    plt.xlim(k_range[0]-1, k_range[-1]+1)

    ymin, ymax = min(sse_list), max(sse_list)
    margin = (ymax - ymin)*0.1
    plt.ylim(ymin - margin, ymax + margin)

    plt.legend(fontsize=10, loc='best')
    plt.tight_layout()

    outpath = CONFIG['RESOURCE_DIR']/f"elbow_plot_{cluster_type}.png"
    plt.savefig(outpath, dpi=120, bbox_inches='tight')
    plt.close()
    logger.info(f"[plot_elbow_graph] 엘보우 그래프 저장 => {outpath}")


def _get_elbow_point(k_range, sse_list):
    """
    kneed 라이브러리 또는 거리방식으로 elbow point 찾기
    """
    if len(k_range) < 3:
        return None

    try:
        elbow_candidate = None
        kl = KneeLocator(k_range, sse_list, curve='convex',
                         direction='decreasing', interp_method='polynomial', S=2.0)
        if kl.knee is not None:
            elbow_candidate = kl.knee

        if elbow_candidate is None:
            # 보조 로직: 첫점-끝점 선분과의 최대 수직 거리
            p1 = np.array([k_range[0], sse_list[0]])
            p2 = np.array([k_range[-1], sse_list[-1]])
            max_dist = -1
            best_k = None
            for k, sse in zip(k_range[1:-1], sse_list[1:-1]):
                px = np.array([k, sse])
                dist = abs(np.cross(p2 - p1, px - p1))/np.linalg.norm(p2 - p1)
                if dist > max_dist:
                    max_dist = dist
                    best_k = k
            elbow_candidate = best_k
        return elbow_candidate

    except Exception as e:
        logger.warning(f"[_get_elbow_point] 에러: {str(e)}")
        return None


def custom_elbow_by_threshold(k_range, sse_list, min_drop_ratio=0.01):
    """
    SSE 감소폭이 초기 SSE 대비 min_drop_ratio 이하가 되면 완만해졌다고 간주
    """
    if len(k_range) < 3:
        return None

    initial_sse = sse_list[0]
    for i in range(2, len(k_range)):
        prev_sse = sse_list[i-1]
        curr_sse = sse_list[i]
        diff = prev_sse - curr_sse
        ratio = diff / initial_sse
        if ratio < min_drop_ratio:
            return k_range[i]
    return None


################################################################################
# 모델 평가
################################################################################
def create_clustering_model(model_type, n_clusters):
    """
    model_type: 'kmeans', 'gmm', 'hierarchical'
    """
    if model_type=='kmeans':
        return KMeans(n_clusters=n_clusters, random_state=CONFIG['RANDOM_SEED'], n_init=15)
    elif model_type=='gmm':
        return GaussianMixture(n_components=n_clusters, n_init=5, random_state=CONFIG['RANDOM_SEED'])
    elif model_type=='hierarchical':
        return AgglomerativeClustering(n_clusters=n_clusters, linkage='ward')
    else:
        raise ValueError(f"Unknown model_type: {model_type}")

def get_labels_by_model(model, model_type, features):
    if model_type in ['kmeans','hierarchical']:
        return model.labels_
    elif model_type=='gmm':
        return model.predict(features)
    else:
        raise ValueError(f"[get_labels_by_model] Unsupported model_type: {model_type}")


def evaluate_models(features, n_clusters):
    """
    KMeans, GMM, Hierarchical 3개 모델을 만들어 각각 composite 점수를 구함
    """
    def _run(mtype):
        try:
            mdl = create_clustering_model(mtype, n_clusters)
            lbs = mdl.fit_predict(features)
            score = compute_composite_score(features, lbs)
            return mtype, {'model':mdl, 'labels':lbs, 'scores':score}
        except Exception as e:
            logger.warning(f"[evaluate_models] {mtype} 실패: {str(e)}")
            return mtype, None

    model_types = ['kmeans','gmm','hierarchical']
    out = Parallel(n_jobs=CONFIG['N_JOBS'], backend=CONFIG['PARALLEL_BACKEND'])(
        delayed(_run)(mt) for mt in model_types
    )
    results = {}
    for mt, val in out:
        if val is not None:
            results[mt] = val
    return results


################################################################################
# 가중치 로드/저장
################################################################################
def resolve_path(file_path):
    p = Path(file_path)
    return p if p.is_absolute() else (CONFIG['RESOURCE_DIR']/p)

def load_weights(cluster_type):
    fname = CONFIG['USER_WEIGHTS_FILE'] if cluster_type=='user' else CONFIG['COUPLE_WEIGHTS_FILE']
    fpath = resolve_path(fname)
    if not fpath.exists():
        logger.warning(f"[load_weights] 파일 없음 => {fpath}")
        return DEFAULT_WEIGHTS[cluster_type]
    try:
        with open(fpath,'r',encoding='utf-8') as f:
            data = json.load(f)
        w = np.array(data['weights'])
        k = data['optimal_k']
        s = data['score']
        return {'weights':w, 'optimal_k':k, 'score':s}
    except Exception as e:
        logger.error(f"[load_weights] 로드 실패: {str(e)}")
        return DEFAULT_WEIGHTS[cluster_type]

def save_weights(ow, cluster_type):
    fname = CONFIG['USER_WEIGHTS_FILE'] if cluster_type=='user' else CONFIG['COUPLE_WEIGHTS_FILE']
    fpath = resolve_path(fname)
    data = {
        'weights': [float(x) for x in ow['weights']],
        'optimal_k': int(ow['optimal_k']),
        'score': float(ow['score'])
    }
    try:
        with open(fpath,'w',encoding='utf-8') as ff:
            json.dump(data, ff, indent=2, ensure_ascii=False)
        logger.info(f"[save_weights] {cluster_type} 가중치 => {fpath}")
    except Exception as e:
        logger.error(f"[save_weights] 실패: {str(e)}")


################################################################################
# K 탐색 (Elbow / Threshold / Composite 종합)
################################################################################
def find_optimal_k(features, k_range, use_elbow=True):
    """
    KMeans 기반으로 SSE, Composite Score를 구하고,
    1) elbow_candidate
    2) threshold_candidate
    3) composite 최대 K
    를 찾은 뒤, 세 가지를 가중합하여 최종 K를 반환
    """
    from sklearn.cluster import KMeans

    def _eval_k(k):
        # KMeans로 inertia(SSE) 및 composite 점수 구함
        km = KMeans(n_clusters=k, random_state=CONFIG['RANDOM_SEED'], n_init=15)
        labels = km.fit_predict(features)
        inertia = km.inertia_
        comp = compute_composite_score(features, labels)['composite']
        return (k, inertia, comp)

    # 1) k_range별 SSE, composite 측정
    results = Parallel(n_jobs=CONFIG['N_JOBS'], backend=CONFIG['PARALLEL_BACKEND'])(
        delayed(_eval_k)(k) for k in k_range
    )
    results.sort(key=lambda x: x[0])
    k_vals   = [r[0] for r in results]
    sse_list = [r[1] for r in results]
    comp_list= [r[2] for r in results]

    # 2) Elbow 찾기
    elbow_k = None
    if use_elbow and len(k_vals)>=3:
        elbow_candidate = _get_elbow_point(k_vals, sse_list)
        if elbow_candidate is not None:
            elbow_k = elbow_candidate

    # 3) Threshold K
    threshold_k = custom_elbow_by_threshold(k_vals, sse_list, min_drop_ratio=0.02)

    # 4) Composite 최대 K
    best_idx = np.argmax(comp_list)
    best_comp_k = k_vals[best_idx]
    logger.info(f"[find_optimal_k] elbow_candidate={elbow_k}, threshold_candidate={threshold_k}, best_comp_k={best_comp_k}")

    # 세 후보가 모두 None일 수도 있으므로 아래 고려
    # 종합 점수 산출을 위한 가중치 설정 (elbow 0.6, threshold 0.2, composite 0.2)
    alpha = 0.6
    beta  = 0.2
    gamma = 0.2

    # 범위를 위한 값
    kmin, kmax = k_range[0], k_range[-1]
    span = kmax - kmin
    max_comp = max(comp_list)

    # 각 K 별로 combined score 계산
    combined_scores = []
    for i, k in enumerate(k_vals):
        # elbowDist, thresholdDist
        # elbowDist = abs(k - elbow_k)
        # thresholdDist = abs(k - threshold_k)
        # (elbow_k, threshold_k 가 None일 수 있으므로 예외처리)

        if elbow_k is not None:
            elbowDist = abs(k - elbow_k)
            elbowScore = 1.0 - (elbowDist/span)  # 0~1로 정규화
        else:
            elbowScore = 0.0

        if threshold_k is not None:
            threshDist = abs(k - threshold_k)
            threshScore= 1.0 - (threshDist/span)
        else:
            threshScore= 0.0

        compNorm = comp_list[i] / max_comp if max_comp>0 else 0.0

        cscore = alpha*elbowScore + beta*threshScore + gamma*compNorm
        combined_scores.append(cscore)

    # 가장 높은 combined score를 갖는 K 선택
    best_k_idx = np.argmax(combined_scores)
    final_k = k_vals[best_k_idx]
    logger.info(f"[find_optimal_k] 최종 선택 K={final_k} (combined_score={combined_scores[best_k_idx]:.4f})")

    return final_k, sse_list, elbow_k


################################################################################
# 가중치 최적화 (Bayesian Optimization)
################################################################################
def optimize_user_weights(features_scaled, n_clusters):
    """
    사용자 클러스터링에 대한 Bayesian Optimization
    """
    loaded_w = load_weights('user')
    if loaded_w and not CONFIG['OPTIMIZE_WEIGHTS']:
        # 최적화 스킵 시, 기존 로드된 가중치 사용
        save_weights(loaded_w, 'user')
        return loaded_w

    if not CONFIG['OPTIMIZE_WEIGHTS']:
        # 최적화 스킵 & 파일도 없으면 기본값
        save_weights(DEFAULT_WEIGHTS['user'],'user')
        return DEFAULT_WEIGHTS['user']

    wr = CONFIG['USER_CLUSTERS']['WEIGHT_RANGES']
    # Real: 연속 실수 범위; prior='log-uniform' 등을 지정해 탐색 스케일 조절 가능
    space = [
        Real(wr['age'][0],     wr['age'][1],     name='age_weight'),
        Real(wr['budget'][0],  wr['budget'][1],  name='budget_weight'),
        Real(wr['mbti_ie'][0], wr['mbti_ie'][1], name='mbti_ie_weight'),
        Real(wr['mbti_sn'][0], wr['mbti_sn'][1], name='mbti_sn_weight'),
        Real(wr['mbti_tf'][0], wr['mbti_tf'][1], name='mbti_tf_weight'),
        Real(wr['mbti_jp'][0], wr['mbti_jp'][1], name='mbti_jp_weight'),
    ]

    @use_named_args(space)
    def objective(**params):
        # 현재 반복에서의 가중치
        w = np.array([
            params['age_weight'],
            params['budget_weight'],
            params['mbti_ie_weight'],
            params['mbti_sn_weight'],
            params['mbti_tf_weight'],
            params['mbti_jp_weight']
        ])
        wf = features_scaled * w

        # 3가지 모델 모두 점수 계산 후, best score
        r = evaluate_models(wf, n_clusters)
        best_score = max(val['scores']['composite'] for val in r.values())
        return -best_score  # gp_minimize는 최소화이므로 음수 반환

    out = gp_minimize(
        objective, space,
        n_calls=CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts=CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state=CONFIG['RANDOM_SEED'],
        n_jobs=CONFIG['N_JOBS']
    )
    best = {
        'weights': np.array(out.x),
        'optimal_k': n_clusters,
        'score': -out.fun
    }
    logger.info(f"[optimize_user_weights] 최적 가중치 => {best}")
    save_weights(best, 'user')
    return best


def optimize_couple_weights(features_scaled, n_clusters):
    """
    커플 클러스터링에 대한 Bayesian Optimization
    """
    loaded_w = load_weights('couple')
    if loaded_w and not CONFIG['OPTIMIZE_WEIGHTS']:
        save_weights(loaded_w,'couple')
        return loaded_w

    if not CONFIG['OPTIMIZE_WEIGHTS']:
        save_weights(DEFAULT_WEIGHTS['couple'],'couple')
        return DEFAULT_WEIGHTS['couple']

    wr = CONFIG['COUPLE_CLUSTERS']['WEIGHT_RANGES']
    space = [
        Real(wr['male_age'][0],            wr['male_age'][1],            name='male_age_weight'),
        Real(wr['female_age'][0],          wr['female_age'][1],          name='female_age_weight'),
        Real(wr['budget'][0],             wr['budget'][1],              name='budget_weight'),
        Real(wr['marriage_month_sin'][0],  wr['marriage_month_sin'][1],  name='marriage_month_sin_weight'),
        Real(wr['marriage_month_cos'][0],  wr['marriage_month_cos'][1],  name='marriage_month_cos_weight'),
        Real(wr['marriage_day_sin'][0],    wr['marriage_day_sin'][1],    name='marriage_day_sin_weight'),
        Real(wr['marriage_day_cos'][0],    wr['marriage_day_cos'][1],    name='marriage_day_cos_weight'),
        Real(wr['age_diff'][0],           wr['age_diff'][1],            name='age_diff_weight'),
        Real(wr['user_cluster_distance'][0], wr['user_cluster_distance'][1], name='user_cluster_distance_weight'),
    ]

    @use_named_args(space)
    def objective(**params):
        w = np.array([
            params['male_age_weight'],
            params['female_age_weight'],
            params['budget_weight'],
            params['marriage_month_sin_weight'],
            params['marriage_month_cos_weight'],
            params['marriage_day_sin_weight'],
            params['marriage_day_cos_weight'],
            params['age_diff_weight'],
            params['user_cluster_distance_weight']
        ])
        wf = features_scaled * w

        r = evaluate_models(wf, n_clusters)
        best_score = max(val['scores']['composite'] for val in r.values())
        return -best_score

    out = gp_minimize(
        objective, space,
        n_calls=CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts=CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state=CONFIG['RANDOM_SEED'],
        n_jobs=CONFIG['N_JOBS']
    )
    best = {
        'weights': np.array(out.x),
        'optimal_k': n_clusters,
        'score': -out.fun
    }
    logger.info(f"[optimize_couple_weights] 최적 가중치 => {best}")
    save_weights(best,'couple')
    return best


################################################################################
# 레이블 매핑 및 결과 저장
################################################################################
def assign_cluster_labels(user_ids, user_labels, couple_ids, couple_labels):
    user_assign = dict(zip(user_ids, user_labels))
    couple_assign = dict(zip(couple_ids, couple_labels))
    return user_assign, couple_assign

def save_scaler_params(scaler_dict, cluster_type):
    """
    학습된 스케일러(평균, 분산, min/max 등) 정보를 JSON으로 저장
    """
    out = {}
    for k, sc in scaler_dict.items():
        if sc is None:
            out[k] = None
        else:
            params = {}
            if hasattr(sc,'mean_'):
                params['mean_'] = sc.mean_.tolist()
            if hasattr(sc,'var_'):
                params['var_']  = sc.var_.tolist()
            if hasattr(sc,'scale_'):
                params['scale_']= sc.scale_.tolist()
            if hasattr(sc,'min_'):
                params['min_']  = sc.min_.tolist()
            if hasattr(sc,'data_min_'):
                params['data_min_'] = sc.data_min_.tolist()
            if hasattr(sc,'data_max_'):
                params['data_max_'] = sc.data_max_.tolist()
            out[k] = params

    fp = CONFIG['RESOURCE_DIR']/f"{cluster_type}_scaler_params.json"
    with open(fp,'w',encoding='utf-8') as f:
        json.dump(out, f, indent=2, ensure_ascii=False)
    logger.info(f"[save_scaler_params] {cluster_type} 스케일러 파라미터 => {fp}")

def get_cluster_centers(model, model_type, features):
    if model_type=='kmeans':
        return model.cluster_centers_
    elif model_type=='gmm':
        return model.means_
    elif model_type=='hierarchical':
        labels = model.labels_
        centers = []
        for lb in np.unique(labels):
            ix = np.where(labels==lb)[0]
            centers.append(features[ix].mean(axis=0))
        return np.array(centers)
    else:
        logger.warning(f"[get_cluster_centers] Unknown model type: {model_type}")
        return None

def save_cluster_centers(cluster_type, model_info, feature_names, weighted_features):
    """
    각 클러스터의 중심점(가중치 적용된 특징의 평균)을 JSON에 저장
    """
    model = model_info['model']
    if isinstance(model, KMeans):
        mt = 'kmeans'
    elif isinstance(model, GaussianMixture):
        mt = 'gmm'
    elif isinstance(model, AgglomerativeClustering):
        mt = 'hierarchical'
    else:
        logger.warning("[save_cluster_centers] 알 수 없는 모델타입")
        return

    centers = get_cluster_centers(model, mt, weighted_features)
    if centers is None:
        return

    out = {}
    for i, cvec in enumerate(centers):
        center_dict = {}
        for fname, val in zip(feature_names, cvec):
            center_dict[fname] = float(val)
        out[f"cluster_{i}"] = center_dict

    fp = CONFIG['RESOURCE_DIR']/f"{cluster_type}_cluster_centers.json"
    with open(fp, 'w', encoding='utf-8') as f:
        json.dump(out, f, indent=2, ensure_ascii=False)
    logger.info(f"[save_cluster_centers] {cluster_type} 클러스터 중심점 => {fp}")

def save_assignments(cluster_type, ids, labels, weighted_features, id_name='userid'):
    arr = []
    for i, ident in enumerate(ids):
        arr.append({
            id_name: int(ident),
            'cluster': int(labels[i]),
            'weighted_features': weighted_features[i].tolist()
        })
    fp = CONFIG['RESOURCE_DIR']/f"{cluster_type}_assignments.json"
    with open(fp,'w',encoding='utf-8') as f:
        json.dump(arr, f, indent=2, ensure_ascii=False)
    logger.info(f"[save_assignments] {cluster_type} 할당 결과 => {fp}")


################################################################################
# JSON 입력 로드
################################################################################
@measure_time
def load_input_json():
    fp = CONFIG['RESOURCE_DIR']/'input_data.json'
    logger.info(f"[load_input_json] 로드 => {fp}")
    with open(fp,'r',encoding='utf-8') as f:
        data = json.load(f)
    if isinstance(data, list):
        data = {'couples': data}
    if 'couples' not in data:
        raise ValueError("[load_input_json] 'couples' 키가 없음")
    logger.info(f"[load_input_json] 커플 수={len(data['couples'])}")
    return data


################################################################################
# 최종 요약 자료 저장
################################################################################
def save_final_summary(result):
    fp = CONFIG['RESOURCE_DIR']/'clustering_summary.json'

    user_scores   = {k: float(v) for k,v in result['user_results']['scores'].items()}
    couple_scores = {k: float(v) for k,v in result['couple_results']['scores'].items()}

    summary = {
        'user_best_model': result['user_results']['best_model_type'],
        'user_scores': user_scores,
        'user_optimal_k': int(result['user_results']['optimal_k']),
        'couple_best_model': result['couple_results']['best_model_type'],
        'couple_scores': couple_scores,
        'couple_optimal_k': int(result['couple_results']['optimal_k']),
        'timestamp': time.strftime('%Y-%m-%d %H:%M:%S')
    }
    with open(fp,'w',encoding='utf-8') as f:
        json.dump(summary, f, indent=2, ensure_ascii=False)
    logger.info(f"[save_final_summary] => {fp}")


################################################################################
# 3D 시각화
################################################################################
def plot_3d_clusters(features, labels, cluster_centers, title, output_path, feature_names=None):
    """
    PCA 또는 t-SNE를 사용하여 3D 상에서 클러스터 시각화
    """
    if not CONFIG['VISUALIZATION']['ENABLED']:
        logger.info("[plot_3d_clusters] 시각화 비활성화됨")
        return

    sns.set_style("whitegrid")
    reduction_method = CONFIG['VISUALIZATION']['REDUCTION_METHOD']

    if reduction_method == 'tsne':
        from sklearn.manifold import TSNE
        emb = TSNE(n_components=3, perplexity=30,
                   random_state=CONFIG['RANDOM_SEED']).fit_transform(features)
        # 중심점도 동일 transform을 적용해야 하나, 여기선 예시로 별도 TSNE 적용
        center_emb = TSNE(n_components=3, perplexity=30,
                          random_state=CONFIG['RANDOM_SEED']).fit_transform(cluster_centers)
        exp_var = [0,0,0]  # t-SNE에선 설명 분산률 의미X

    else:
        from sklearn.decomposition import PCA
        pca = PCA(n_components=3)
        emb = pca.fit_transform(features)
        center_emb = pca.transform(cluster_centers)
        exp_var = pca.explained_variance_ratio_ * 100

    fig = plt.figure(figsize=(9,7), dpi=120)
    ax = fig.add_subplot(111, projection='3d')

    n_clusters = len(np.unique(labels))
    color_set = sns.color_palette('hls', n_clusters)

    for cidx in range(n_clusters):
        mask = (labels==cidx)
        ax.scatter(
            emb[mask,0], emb[mask,1], emb[mask,2],
            c=[color_set[cidx]],
            label=f'Cluster {cidx}',
            alpha=0.7, s=40, edgecolor='none'
        )

    # 중심점
    ax.scatter(
        center_emb[:,0], center_emb[:,1], center_emb[:,2],
        c='red', marker='*', s=300, label='Centroids',
        edgecolor='black'
    )

    total_var = sum(exp_var)
    ax.set_title(f"{title}\n(method={reduction_method}, Expl.Var={total_var:.1f}%)",
                 fontsize=13, fontweight='bold')
    if reduction_method=='pca':
        ax.set_xlabel(f'PC1 ({exp_var[0]:.1f}%)', fontsize=11, labelpad=10)
        ax.set_ylabel(f'PC2 ({exp_var[1]:.1f}%)', fontsize=11, labelpad=10)
        ax.set_zlabel(f'PC3 ({exp_var[2]:.1f}%)', fontsize=11, labelpad=10)
    else:
        ax.set_xlabel('Dim1', fontsize=11, labelpad=10)
        ax.set_ylabel('Dim2', fontsize=11, labelpad=10)
        ax.set_zlabel('Dim3', fontsize=11, labelpad=10)

    ax.view_init(elev=20, azim=45)
    ax.legend(loc='best')
    plt.tight_layout()

    plt.savefig(output_path, dpi=CONFIG['VISUALIZATION']['DPI'], bbox_inches='tight')
    plt.close()
    logger.info(f"[plot_3d_clusters] 3D 시각화 => {output_path}")


################################################################################
# 메인 함수
################################################################################
@measure_time
def main(config=CONFIG):
    logger.info("=== 클러스터링 프로세스 시작 ===")

    try:
        # 1) 데이터 로드
        if config['USE_DUMMY_DATA']:
            couples_json = create_dummy_couples_json(n_couples=config['SAMPLE_SIZE'])
            logger.info(f"[main] 더미데이터 커플={len(couples_json['couples'])}")
        else:
            couples_json = load_input_json()
            logger.info(f"[main] 실데이터 커플={len(couples_json['couples'])}")

        # 2) NumPy 변환
        user_arr, user_ids = users_to_numpy(couples_json)
        couple_arr, couple_ids, female_ids, male_ids = couples_to_numpy(couples_json)

        # 3) 사용자 스케일링
        user_scaled, user_scalers = scale_user_features(user_arr)

        # 4) 사용자 K 탐색
        if config['SKIP_OPTIMIZE_K']:
            user_k = config['USER_CLUSTERS']['N_CLUSTERS']
            sse_list_user = []
            elbow_user = None
            logger.info(f"[main] 사용자 K 탐색 스킵 => {user_k}")
        else:
            user_k, sse_list_user, elbow_user = find_optimal_k(
                user_scaled,
                config['USER_K_RANGE'],
                use_elbow=True
            )

        # 5) 사용자 가중치 최적화
        user_opt_w = optimize_user_weights(user_scaled, user_k)
        weighted_user_feat = user_scaled*user_opt_w['weights']

        # 사용자 모델 평가 -> 최적 모델 선택
        user_models = evaluate_models(weighted_user_feat, user_opt_w['optimal_k'])
        best_user_model_name, best_user_data = max(user_models.items(), key=lambda x:x[1]['scores']['composite'])
        user_labels = get_labels_by_model(best_user_data['model'], best_user_model_name, user_scaled)
        logger.info(f"[사용자] Best Model={best_user_model_name}, Scores={best_user_data['scores']}")

        # 스케일러 파라미터 저장
        save_scaler_params(user_scalers, 'user')

        # 엘보우 그래프
        if config['PLOT_ELBOW_GRAPHS'] and not config['SKIP_OPTIMIZE_K']:
            plot_elbow_graph(config['USER_K_RANGE'], sse_list_user, elbow_user, 'user')

        # 6) user_cluster_distance (여성 vs 남성 유저 가중치 벡터 거리)
        idx_map = {uid:i for i, uid in enumerate(user_ids)}
        dist_list = []
        for i in range(len(couple_ids)):
            fid = female_ids[i]
            mid = male_ids[i]
            if fid in idx_map and mid in idx_map:
                fvec = weighted_user_feat[idx_map[fid]]
                mvec = weighted_user_feat[idx_map[mid]]
                dist = np.linalg.norm(fvec - mvec)
            else:
                dist = 0.0
            dist_list.append(dist)

        dist_arr = np.array(dist_list)
        couple_arr[:,8] = dist_arr  # user_cluster_distance 추가

        # 7) 커플 스케일링
        couple_scaled, couple_scalers = scale_couple_features(couple_arr)

        # 8) 커플 K 탐색
        if config['SKIP_OPTIMIZE_K']:
            couple_k = config['COUPLE_CLUSTERS']['N_CLUSTERS']
            sse_list_couple=[]
            elbow_couple=None
            logger.info(f"[main] 커플 K 탐색 스킵 => {couple_k}")
        else:
            couple_k, sse_list_couple, elbow_couple = find_optimal_k(
                couple_scaled,
                config['COUPLE_K_RANGE'],
                use_elbow=True
            )

        # 9) 커플 가중치 최적화
        couple_opt_w = optimize_couple_weights(couple_scaled, couple_k)
        weighted_couple_feat = couple_scaled*couple_opt_w['weights']

        # 커플 모델 평가 -> 최적 모델 선정
        couple_models = evaluate_models(weighted_couple_feat, couple_opt_w['optimal_k'])
        best_couple_model_name, best_couple_data = max(couple_models.items(), key=lambda x:x[1]['scores']['composite'])
        couple_labels = get_labels_by_model(best_couple_data['model'], best_couple_model_name, couple_scaled)
        logger.info(f"[커플] Best Model={best_couple_model_name}, Scores={best_couple_data['scores']}")

        # 스케일러 파라미터 저장
        save_scaler_params(couple_scalers, 'couple')

        # 커플 엘보우 그래프
        if config['PLOT_ELBOW_GRAPHS'] and not config['SKIP_OPTIMIZE_K']:
            plot_elbow_graph(config['COUPLE_K_RANGE'], sse_list_couple, elbow_couple, 'couple')

        # 10) 레이블 할당
        user_assign, couple_assign = assign_cluster_labels(user_ids, user_labels, couple_ids, couple_labels)

        # 11) 클러스터 중심점
        save_cluster_centers(
            'user',
            user_models[best_user_model_name],
            config['USER_CLUSTERS']['FEATURES'],
            weighted_user_feat
        )
        save_cluster_centers(
            'couple',
            couple_models[best_couple_model_name],
            config['COUPLE_CLUSTERS']['FEATURES'],
            weighted_couple_feat
        )

        # 12) 할당 결과
        save_assignments('user', user_ids, user_labels, weighted_user_feat, id_name='userid')
        save_assignments('couple', couple_ids, couple_labels, weighted_couple_feat, id_name='coupleid')

        # 13) 3D 시각화
        if config['VISUALIZATION']['ENABLED']:
            # 사용자 시각화
            user_centers = get_cluster_centers(best_user_data['model'], best_user_model_name, weighted_user_feat)
            plot_3d_clusters(
                weighted_user_feat, user_labels, user_centers,
                title=config['VISUALIZATION']['USER_FEATURES']['title'],
                output_path=config['RESOURCE_DIR']/config['VISUALIZATION']['USER_FEATURES']['filename'],
                feature_names=config['USER_CLUSTERS']['FEATURES']
            )
            # 커플 시각화
            couple_centers = get_cluster_centers(best_couple_data['model'], best_couple_model_name, weighted_couple_feat)
            plot_3d_clusters(
                weighted_couple_feat, couple_labels, couple_centers,
                title=config['VISUALIZATION']['COUPLE_FEATURES']['title'],
                output_path=config['RESOURCE_DIR']/config['VISUALIZATION']['COUPLE_FEATURES']['filename'],
                feature_names=config['COUPLE_CLUSTERS']['FEATURES']
            )

        logger.info("=== 클러스터링 프로세스 완료 ===")

        # 최종 요약
        result = {
            'user_results': {
                'best_model_type': best_user_model_name,
                'scores': best_user_data['scores'],
                'optimal_k': user_k
            },
            'couple_results': {
                'best_model_type': best_couple_model_name,
                'scores': best_couple_data['scores'],
                'optimal_k': couple_k
            },
            'assignments': {
                'user': user_assign,
                'couple': couple_assign
            }
        }
        save_final_summary(result)
        return result

    except Exception as e:
        logger.exception(f"[main] 예외발생: {str(e)}")
        raise


################################################################################
# 스크립트 직접 실행 시
################################################################################
if __name__=="__main__":
    if len(sys.argv) > 1:
        # 첫 번째 인자가 true/false라면 더미데이터 여부 결정
        CONFIG['USE_DUMMY_DATA'] = (sys.argv[1].lower()=='true')

    os.makedirs(CONFIG['RESOURCE_DIR'], exist_ok=True)
    try:
        res = main(CONFIG)
        logger.info("[__main__] 완료")
        sys.exit(0)
    except FileNotFoundError as fe:
        logger.exception(f"[__main__] 파일 없음: {fe}")
        sys.exit(1)
    except Exception as e:
        logger.exception(f"[__main__] 에러: {e}")
        sys.exit(1)