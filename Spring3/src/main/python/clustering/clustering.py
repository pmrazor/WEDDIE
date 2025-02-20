"""
[웨딩 추천 시스템 - 클러스터링 모듈]

주요 기능:
1. 사용자/커플 데이터 클러스터링
2. 최적 군집 수(K) 탐색
3. 특성 가중치 최적화
4. 시각화 및 결과 저장

프로세스:
1. 데이터 전처리 (스케일링, 인코딩)
2. Elbow 방법으로 최적 K 탐색
3. 다양한 클러스터링 모델 평가
4. Bayesian Optimization으로 가중치 최적화
5. 결과 저장 및 시각화
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
import matplotlib.pyplot as plt
import seaborn as sns

from sklearn.preprocessing import StandardScaler, MinMaxScaler
from sklearn.cluster import KMeans, AgglomerativeClustering
from sklearn.mixture import GaussianMixture
from sklearn.metrics import silhouette_score, davies_bouldin_score, calinski_harabasz_score
from scipy.spatial.distance import cdist, pdist
from kneed import KneeLocator
from sklearn.manifold import TSNE
import multiprocessing
import warnings

# joblib 경고 숨기기
warnings.filterwarnings(
    action="ignore",
    message="Could not find the number of physical cores",
    category=UserWarning,
    module="joblib.externals.loky"
)

# (1) 전역 설정
##############################################################################
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_RESOURCE_DIR = SCRIPT_DIR.parent.parent.parent.parent / 'data' / 'resources'

CONFIG = {
    'USE_DUMMY_DATA': False,       # True면 더미데이터 생성
    'SAMPLE_SIZE': 2000,          # 더미 커플 수
    'RANDOM_SEED': 42,            # 난수 고정

    # 군집 평가용 K 범위
    'USER_K_RANGE': range(2, 31),
    'COUPLE_K_RANGE': range(2, 31),

    'OPTIMIZE_WEIGHTS': False,
    'PLOT_ELBOW_GRAPHS': True,

    # Elbow가 없으면 고정 K 사용
    'USE_ELBOW_K': True,
    'FIXED_USER_K': 8,
    'FIXED_COUPLE_K': 8,

    # KneeLocator 민감도 
    'KNEE_SENSITIVITY': 5.0,

    # 결과 저장 위치
    'RESOURCE_DIR': DEFAULT_RESOURCE_DIR,
    'USER_WEIGHTS_FILE': 'user_optimized_weights.json',
    'COUPLE_WEIGHTS_FILE': 'couple_optimized_weights.json',

    # 사용자 특성 및 가중치 범위
    'USER_CLUSTERS': {
        'FEATURES': [
            'age','budget','mbti_ie','mbti_sn','mbti_tf','mbti_jp'
        ],
        'WEIGHT_RANGES': {
            'age': (1.0, 6.0),
            'budget': (3.0, 16.0), 
            'mbti_ie': (0.5, 5.0),
            'mbti_sn': (0.5, 5.0),
            'mbti_tf': (0.5, 5.0),
            'mbti_jp': (0.5, 5.0)
        }
    },

    # 커플 특성 및 가중치 범위
    'COUPLE_CLUSTERS': {
        'FEATURES': [
            'male_age','female_age','budget',
            'marriage_month_sin','marriage_month_cos',
            'marriage_day_sin','marriage_day_cos',
            'age_diff','user_cluster_distance'
        ],
        'WEIGHT_RANGES': {
            'male_age': (1.0, 6.0),
            'female_age': (1.0, 6.0),
            'budget': (4.0, 16.0),
            'marriage_month_sin': (0.5, 6.0),
            'marriage_month_cos': (0.5, 6.0),
            'marriage_day_sin': (0.1, 0.5),
            'marriage_day_cos': (0.1, 0.5),
            'age_diff': (1.0, 8.0),
            'user_cluster_distance': (0.5, 8.0)
        }
    },

    # Bayesian Optimization 파라미터
    'OPTIMIZE_N_CALLS': 60,
    'OPTIMIZATION_N_RANDOM_STARTS': 30,

    # 병렬
    'N_JOBS': max(1, multiprocessing.cpu_count() - 1),
    'PARALLEL_BACKEND': 'loky',

    # 시각화
    'VISUALIZATION': {
        'ENABLED': True,
        'MODE': 'custom_tsne',
        'DPI': 120,
        'MARKER_SIZE': 35,
        'MARKER_ALPHA': 0.7,
        'USER_FEATURES': {
            'title': '3D User Clusters',
            'filename': 'user_clusters_3d.png'
        },
        'COUPLE_FEATURES': {
            'title': '3D Couple Clusters',
            'filename': 'couple_clusters_3d.png'
        }
    },
}

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


# (2) 로깅 설정
##############################################################################
def setup_logging():
    """
    로깅 설정
    - 파일과 콘솔에 동시 출력
    - 시간, 레벨, 메시지 포맷 지정
    """
    log_dir = CONFIG['RESOURCE_DIR'] / 'logs'
    log_dir.mkdir(parents=True, exist_ok=True)
    
    log_file = log_dir / f'clustering_{time.strftime("%Y%m%d_%H%M%S")}.log'
    
    f_handler = logging.FileHandler(log_file, encoding='utf-8')
    f_handler.setLevel(logging.INFO)
    f_handler.setFormatter(
        logging.Formatter('[%(asctime)s]-[%(levelname)s] %(message)s','%Y-%m-%d %H:%M:%S')
    )
    c_handler = logging.StreamHandler()
    c_handler.setLevel(logging.INFO)
    c_handler.setFormatter(
        logging.Formatter('[%(asctime)s]-[%(levelname)s] %(message)s','%Y-%m-%d %H:%M:%S')
    )
    
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(f_handler)
    logger.addHandler(c_handler)
    return logger

logger = setup_logging()

def measure_time(func):
    """
    함수 실행 시간 측정 데코레이터
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        st = time.time()
        r = func(*args, **kwargs)
        et = time.time()
        logger.info(f"[TIME] {func.__name__} 완료: {et - st:.2f}초")
        return r
    return wrapper


# (3) 더미 데이터 생성
##############################################################################
def create_distinct_dummy_couples_json(n_couples=2000):
    """
    테스트용 더미 커플 데이터 생성
    - 뚜렷한 패턴을 가진 그룹으로 생성
    - 나이, 예산, MBTI, 결혼날짜 등 포함
    """
    np.random.seed(CONFIG['RANDOM_SEED'])
    couples_data = {"couples": []}

    MBTI_PATTERNS = [
        ('ISTJ','ISFJ'), ('ESTP','ESFP'), ('INFJ','INFP'),
        ('INTJ','INTP'), ('ESTJ','ENTJ'), ('ISTP','ISFP'),
        ('ESFJ','ENFJ'), ('ENTP','ENFP'), ('ENFP','ENFJ'),
        ('ISFP','ISTP')
    ]
    AGE_PATTERNS = [
        ((23,25),(25,27)), ((24,26),(31,33)), ((27,29),(29,31)),
        ((30,32),(32,35)), ((33,35),(35,38)), ((28,30),(40,42)),
        ((24,26),(40,42)), ((35,37),(38,40)), ((37,39),(45,47)),
        ((27,29),(48,50))
    ]
    BUDGET_PATTERNS = [
        (2000,3000), (3100,4000), (4100,5000), (5100,6000), (6100,7000),
        (7100,8000), (8100,9000), (9100,10500), (10600,12000), (12100,14000)
    ]
    DATE_PATTERNS = [
        ((2,3),(10,15)),((4,5),(1,5)),((6,7),(6,10)),((8,9),(11,15)),
        ((10,11),(1,5)),((3,4),(20,25)),((5,6),(20,25)),((7,8),(20,25)),
        ((9,10),(20,25)),((11,12),(10,15))
    ]
    
    num_patterns = len(MBTI_PATTERNS)
    cpls_per_pattern = n_couples // num_patterns
    remainder = n_couples % num_patterns

    couple_id= 1
    for p_idx in range(num_patterns):
        batch_size = cpls_per_pattern + (1 if p_idx<remainder else 0)

        f_mbti, m_mbti = MBTI_PATTERNS[p_idx]
        (f_lo,f_hi),(m_lo,m_hi) = AGE_PATTERNS[p_idx]
        bud_lo,bud_hi = BUDGET_PATTERNS[p_idx]
        ( (mon_lo,mon_hi),(day_lo,day_hi) )= DATE_PATTERNS[p_idx]

        for _ in range(batch_size):
            f_age_float = np.random.randint(f_lo, f_hi+1) + np.random.normal(0,5)
            f_age = int(max(f_age_float, 1))

            m_age_float = np.random.randint(m_lo, m_hi+1) + np.random.normal(0,5)
            m_age = int(max(m_age_float, 1))
            
            if m_age<f_age:
                m_age = f_age + np.random.randint(0,3)

            base_budget = np.random.randint(bud_lo,bud_hi+1)*10000
            budget = int(base_budget + np.random.normal(0,350000))
            budget = max(budget, 100000)

            mm= np.random.randint(mon_lo, mon_hi+1)
            dd= np.random.randint(day_lo, day_hi+1)

            couple_data = {
                "couple_id": couple_id,
                "female": {
                    "user_id": couple_id*2-1,
                    "age": int(f_age),
                    "mbti": f_mbti
                },
                "male": {
                    "user_id": couple_id*2,
                    "age": int(m_age),
                    "mbti": m_mbti
                },
                "budget": int(budget),
                "marriage_date": f"{mm:02d}-{dd:02d}"
            }
            couples_data["couples"].append(couple_data)
            couple_id+=1

    return couples_data


# (4) MBTI/날짜 인코딩
##############################################################################
def mbti_to_encoding(mbti_str):
    """
    MBTI 문자열을 이진 인코딩으로 변환
    예: 'ISTJ' -> (0,0,0,0)
    """
    if not mbti_str or len(mbti_str)!=4:
        return (0,0,0,0)
    mbti_str = mbti_str.upper()
    return (
        1 if mbti_str[0]=='E' else 0,
        1 if mbti_str[1]=='N' else 0,
        1 if mbti_str[2]=='F' else 0,
        1 if mbti_str[3]=='P' else 0
    )

def encode_cyclic_date(date_str):
    """
    날짜를 순환 인코딩으로 변환
    - sin/cos 변환으로 순환성 표현
    """
    try:
        mm, dd= date_str.split('-')
        mm,dd= int(mm),int(dd)
        m_sin= math.sin(2*math.pi*(mm-1)/12)
        m_cos= math.cos(2*math.pi*(mm-1)/12)
        d_sin= math.sin(2*math.pi*(dd-1)/31)
        d_cos= math.cos(2*math.pi*(dd-1)/31)
        return (m_sin,m_cos,d_sin,d_cos)
    except:
        return (0,1,0,1)


# (5) JSON -> NumPy
##############################################################################
def convert_users_to_numpy(couples_json):
    """
    사용자 JSON 데이터를 NumPy 배열로 변환
    - 나이, 예산, MBTI 특성 추출
    """
    cpls= couples_json['couples']
    N= len(cpls)
    out= np.zeros((2*N,6), dtype=float)
    ids= np.zeros(2*N, dtype=int)
    for i,c in enumerate(cpls):
        f_ix= i*2
        m_ix= i*2+1
        fmb= mbti_to_encoding(c['female']['mbti'])
        mmb= mbti_to_encoding(c['male']['mbti'])

        out[f_ix]= [
            c['female']['age'],
            c['budget'],
            fmb[0],fmb[1],fmb[2],fmb[3]
        ]
        out[m_ix]= [
            c['male']['age'],
            c['budget'],
            mmb[0],mmb[1],mmb[2],mmb[3]
        ]
        ids[f_ix]= c['female']['user_id']
        ids[m_ix]= c['male']['user_id']
    return out, ids

def convert_couples_to_numpy(couples_json):
    cpls= couples_json['couples']
    N= len(cpls)
    out= np.zeros((N,9), dtype=float)
    cids= np.zeros(N, dtype=int)
    fids= np.zeros(N, dtype=int)
    mids= np.zeros(N, dtype=int)
    for i,c in enumerate(cpls):
        cids[i]= c['couple_id']
        fids[i]= c['female']['user_id']
        mids[i]= c['male']['user_id']

        msin,mcos,dsin,dcos= encode_cyclic_date(c['marriage_date'])
        diff= c['male']['age']- c['female']['age']
        out[i]= [
            c['male']['age'],
            c['female']['age'],
            c['budget'],
            msin,mcos,
            dsin,dcos,
            diff,
            0.0
        ]
    return out,cids,fids,mids


# (6) 스케일링
##############################################################################
def scale_user_features(uarr):
    X= uarr.copy()
    sc_age= StandardScaler()
    sc_bud= MinMaxScaler()
    X[:,0:1]= sc_age.fit_transform(X[:,0:1])
    X[:,1:2]= sc_bud.fit_transform(X[:,1:2])

    scalers= {
        'age': sc_age,
        'budget': sc_bud,
        'mbti_ie': None,
        'mbti_sn': None,
        'mbti_tf': None,
        'mbti_jp': None
    }
    return X, scalers

def scale_couple_features(carr):
    X= carr.copy()
    sc_ma= StandardScaler()
    sc_fa= StandardScaler()
    sc_bud= MinMaxScaler()
    sc_msin=MinMaxScaler()
    sc_mcos=MinMaxScaler()
    sc_dsin=MinMaxScaler()
    sc_dcos=MinMaxScaler()
    sc_adiff=MinMaxScaler()
    sc_udist=MinMaxScaler()

    X[:,0:1]= sc_ma.fit_transform(X[:,0:1])
    X[:,1:2]= sc_fa.fit_transform(X[:,1:2])
    X[:,2:3]= sc_bud.fit_transform(X[:,2:3])
    X[:,3:4]= sc_msin.fit_transform(X[:,3:4])
    X[:,4:5]= sc_mcos.fit_transform(X[:,4:5])
    X[:,5:6]= sc_dsin.fit_transform(X[:,5:6])
    X[:,6:7]= sc_dcos.fit_transform(X[:,6:7])
    X[:,7:8]= sc_adiff.fit_transform(X[:,7:8])
    X[:,8:9]= sc_udist.fit_transform(X[:,8:9])

    scalers= {
        'male_age': sc_ma,
        'female_age': sc_fa,
        'budget': sc_bud,
        'marriage_month_sin': sc_msin,
        'marriage_month_cos': sc_mcos,
        'marriage_day_sin': sc_dsin,
        'marriage_day_cos': sc_dcos,
        'age_diff': sc_adiff,
        'user_cluster_distance': sc_udist
    }
    return X, scalers


# (7) 군집 평가
##############################################################################
def compute_dunn_index(X, lbs):
    labs= np.unique(lbs)
    if len(labs)<2:
        return 0
    clusters= [np.where(lbs==l)[0] for l in labs]
    min_inter= float('inf')
    max_intra=0

    for i, idx_i in enumerate(clusters):
        Xi= X[idx_i]
        if len(Xi)>1:
            intra= pdist(Xi).max()
            if intra>max_intra:
                max_intra= intra
        for j in range(i+1,len(clusters)):
            Xj= X[clusters[j]]
            inter= cdist(Xi,Xj).min()
            if inter<min_inter:
                min_inter= inter
    if max_intra==0:
        return 0
    return float(min_inter/max_intra)

def compute_composite_score(X, lbs):
    s= silhouette_score(X, lbs)
    db= davies_bouldin_score(X, lbs)
    ch= calinski_harabasz_score(X, lbs)
    dn= compute_dunn_index(X, lbs)

    s_norm= (s+1)/2
    db_norm= 1/(db+1)
    ch_norm= min(ch/10000,1)
    dn_norm= min(dn,1)
    comp= 0.4*s_norm + 0.2*db_norm + 0.2*ch_norm + 0.2*dn_norm
    return {
        'composite': comp,
        'silhouette': s,
        'davies_bouldin': db,
        'calinski_harabasz': ch,
        'dunn_index': dn
    }


# (8) 모델 생성/평가
##############################################################################
def create_clustering_model(model_type, k):
    if model_type=='kmeans':
        return KMeans(n_clusters=k,random_state=CONFIG['RANDOM_SEED'], n_init=10)
    elif model_type=='gmm':
        return GaussianMixture(n_components=k, random_state=CONFIG['RANDOM_SEED'], n_init=5)
    elif model_type=='hierarchical':
        return AgglomerativeClustering(n_clusters=k, linkage='ward')
    else:
        raise ValueError("Unknown model type.")

def get_labels_by_model(mdl, mtype, X):
    if mtype in ['kmeans','hierarchical']:
        return mdl.labels_
    elif mtype=='gmm':
        return mdl.predict(X)
    else:
        raise ValueError("model type not supported")

def evaluate_models(X, k):
    from joblib import Parallel, delayed
    model_types= ['kmeans','gmm','hierarchical']
    def _do(mt):
        try:
            model= create_clustering_model(mt,k)
            lbs= model.fit_predict(X)
            scores= compute_composite_score(X,lbs)
            return mt, {'model':model,'labels':lbs,'scores':scores}
        except Exception as e:
            logger.warning(f"[evaluate_models] fail {mt}: {e}")
            return mt,None
    outs= Parallel(n_jobs=CONFIG['N_JOBS'], backend=CONFIG['PARALLEL_BACKEND'])(delayed(_do)(m) for m in model_types)
    ret={}
    for mt,val in outs:
        if val is not None:
            ret[mt]= val
    return ret


# (9) Elbow
##############################################################################
def find_elbow_point(k_range, sse_list):
    if len(k_range)<3:
        return None
    kl= KneeLocator(
        k_range, sse_list,
        curve='convex', direction='decreasing',
        S=3.0, interp_method='polynomial'
    )
    return kl.knee

def plot_elbow_graph(k_range, sse_list, elbow_k, title=''):
    sns.set_style('whitegrid')
    plt.figure(figsize=(6,4), dpi=120)
    plt.plot(k_range, sse_list, '-o', color='blue', markersize=6, linewidth=2, label='SSE')
    if elbow_k and elbow_k in k_range:
        idx= k_range.index(elbow_k)
        val= sse_list[idx]
        plt.plot(elbow_k,val,'ro', label='Elbow')
        plt.annotate(f"Elbow={elbow_k}",
                     xy=(elbow_k,val),
                     xytext=(elbow_k+0.4, val+(max(sse_list)-min(sse_list))*0.06),
                     arrowprops=dict(facecolor='red', shrink=0.05),
                     fontsize=10, color='red')
    plt.title(f'Elbow Plot ({title})', fontsize=13, fontweight='bold')
    plt.xlabel('Number of Clusters (k)')
    plt.ylabel('SSE')
    plt.legend(loc='best')
    plt.tight_layout()
    return plt.gcf()


# (10) 가중치 로드/저장 + Bayesian
##############################################################################
def load_weights(ctype):
    fname= CONFIG['USER_WEIGHTS_FILE'] if ctype=='user' else CONFIG['COUPLE_WEIGHTS_FILE']
    fpath= CONFIG['RESOURCE_DIR']/fname
    if not fpath.exists():
        return DEFAULT_WEIGHTS[ctype]
    try:
        with open(fpath,'r',encoding='utf-8') as ff:
            data= json.load(ff)
        w= np.array(data['weights'])
        return {
            'weights': w,
            'optimal_k': int(data['optimal_k']),
            'score': float(data['score'])
        }
    except:
        return DEFAULT_WEIGHTS[ctype]

def save_weights(ow, ctype):
    fname= CONFIG['USER_WEIGHTS_FILE'] if ctype=='user' else CONFIG['COUPLE_WEIGHTS_FILE']
    fpath= CONFIG['RESOURCE_DIR']/fname
    dd= {
        'weights': [float(x) for x in ow['weights']],
        'optimal_k': int(ow['optimal_k']),
        'score': float(ow['score'])
    }
    with open(fpath,'w',encoding='utf-8') as ff:
        json.dump(dd, ff, indent=2, ensure_ascii=False)

from skopt import gp_minimize
from skopt.space import Real
from skopt.utils import use_named_args

def optimize_user_weights(X, k):
    if not CONFIG['OPTIMIZE_WEIGHTS']:
        lw= load_weights('user')
        lw['optimal_k']= k
        save_weights(lw,'user')
        return lw
    
    wr = CONFIG['USER_CLUSTERS']['WEIGHT_RANGES']
    space= [
        Real(wr['age'][0], wr['age'][1], name='age'),
        Real(wr['budget'][0], wr['budget'][1], name='budget'),
        Real(wr['mbti_ie'][0], wr['mbti_ie'][1], name='mbti_ie'),
        Real(wr['mbti_sn'][0], wr['mbti_sn'][1], name='mbti_sn'),
        Real(wr['mbti_tf'][0], wr['mbti_tf'][1], name='mbti_tf'),
        Real(wr['mbti_jp'][0], wr['mbti_jp'][1], name='mbti_jp'),
    ]
    @use_named_args(space)
    def objective(**params):
        w= np.array([
            params['age'],
            params['budget'],
            params['mbti_ie'],
            params['mbti_sn'],
            params['mbti_tf'],
            params['mbti_jp']
        ])
        Xw= X*w
        r= evaluate_models(Xw,k)
        best= max(v['scores']['composite'] for v in r.values())
        return -best
    out= gp_minimize(
        objective, space,
        n_calls= CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts= CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state= CONFIG['RANDOM_SEED'],
        n_jobs= CONFIG['N_JOBS']
    )
    best= {
        'weights': np.array(out.x),
        'optimal_k': k,
        'score': -out.fun
    }
    save_weights(best,'user')
    return best

def optimize_couple_weights(X, k):
    if not CONFIG['OPTIMIZE_WEIGHTS']:
        lw= load_weights('couple')
        lw['optimal_k']=k
        save_weights(lw,'couple')
        return lw
    
    wr = CONFIG['COUPLE_CLUSTERS']['WEIGHT_RANGES']
    space= [
        Real(wr['male_age'][0], wr['male_age'][1], name='male_age'),
        Real(wr['female_age'][0], wr['female_age'][1], name='female_age'),
        Real(wr['budget'][0], wr['budget'][1], name='budget'),
        Real(wr['marriage_month_sin'][0], wr['marriage_month_sin'][1], name='marriage_month_sin'),
        Real(wr['marriage_month_cos'][0], wr['marriage_month_cos'][1], name='marriage_month_cos'),
        Real(wr['marriage_day_sin'][0], wr['marriage_day_sin'][1], name='marriage_day_sin'),
        Real(wr['marriage_day_cos'][0], wr['marriage_day_cos'][1], name='marriage_day_cos'),
        Real(wr['age_diff'][0], wr['age_diff'][1], name='age_diff'),
        Real(wr['user_cluster_distance'][0], wr['user_cluster_distance'][1], name='user_cluster_distance'),
    ]
    @use_named_args(space)
    def objective(**params):
        w= np.array([
            params['male_age'],
            params['female_age'],
            params['budget'],
            params['marriage_month_sin'],
            params['marriage_month_cos'],
            params['marriage_day_sin'],
            params['marriage_day_cos'],
            params['age_diff'],
            params['user_cluster_distance']
        ])
        Xw= X*w
        r= evaluate_models(Xw,k)
        best= max(val['scores']['composite'] for val in r.values())
        return -best
    out= gp_minimize(
        objective, space,
        n_calls= CONFIG['OPTIMIZE_N_CALLS'],
        n_random_starts= CONFIG['OPTIMIZATION_N_RANDOM_STARTS'],
        random_state= CONFIG['RANDOM_SEED'],
        n_jobs= CONFIG['N_JOBS']
    )
    best= {
        'weights': np.array(out.x),
        'optimal_k': k,
        'score': -out.fun
    }
    save_weights(best,'couple')
    return best


# (11) 군집 결과 저장
##############################################################################
def assign_cluster_labels(uids, ulbs, cids, clbs):
    return dict(zip(uids,ulbs)), dict(zip(cids,clbs))

def get_cluster_centers(model, mtype, X):
    if mtype=='kmeans':
        return model.cluster_centers_
    elif mtype=='gmm':
        return model.means_
    elif mtype=='hierarchical':
        lbs= model.labels_
        centers=[]
        for lb in np.unique(lbs):
            centers.append(X[lbs==lb].mean(axis=0))
        return np.array(centers)
    return None

def save_cluster_centers(ctype, best_info, feat_names, wX):
    """
    ctype: 'user' or 'couple'
    best_info: {'model':..., 'labels':..., 'scores':...}
    feat_names: 해당 데이터의 특성명 (예: ['age','budget','mbti_ie',...])
    wX: 가중치 적용된 최종 벡터
    """
    model= best_info['model']
    if isinstance(model,KMeans):
        mtype='kmeans'
    elif isinstance(model,GaussianMixture):
        mtype='gmm'
    elif isinstance(model,AgglomerativeClustering):
        mtype='hierarchical'
    else:
        mtype='unknown'
    cc= get_cluster_centers(model,mtype,wX)
    if cc is None:
        return
    out={}
    for i, cvec in enumerate(cc):
        dd={}
        for fn,val in zip(feat_names,cvec):
            dd[fn]= float(val)
        out[f"cluster_{i}"]= dd

    # user_cluster_centers.json, couple_cluster_centers.json 형식으로 저장
    fp= CONFIG['RESOURCE_DIR']/f"{ctype}_cluster_centers.json"
    with open(fp,'w',encoding='utf-8') as ff:
        json.dump(out,ff,indent=2,ensure_ascii=False)

def save_assignments(ctype, ids, labels, wX, idname='userid'):
    """
    ctype: 'user' or 'couple'
    ids: 사용자(또는 커플) id 배열
    labels: 할당된 군집 라벨
    wX: 가중치 적용된 feature 벡터
    idname: 키 이름('userid' 또는 'coupleid')
    """
    arr=[]
    for i, ident in enumerate(ids):
        arr.append({
            idname: int(ident),
            'cluster': int(labels[i]),
            'weighted_features': wX[i].tolist()
        })
    fp= CONFIG['RESOURCE_DIR']/f"{ctype}_assignments.json"
    with open(fp,'w',encoding='utf-8') as ff:
        json.dump(arr,ff,indent=2,ensure_ascii=False)

def save_final_summary(rdict):
    fp= CONFIG['RESOURCE_DIR']/ 'clustering_summary.json'
    with open(fp,'w',encoding='utf-8') as ff:
        json.dump(rdict,ff,indent=2, ensure_ascii=False)


# (12) 3D 시각화 - User(Naive) / Couple(나이·예산·날짜+dist)
##############################################################################
def plot_3d_user_custom(user_wX, labels, title, outpath):
    """
    x축: user_wX[:,0]  => 나이(스케일)
    y축: user_wX[:,1]  => 예산(스케일)
    z축: MBTI 4차원을 TSNE(1차원) => (N, )
    """
    if not CONFIG['VISUALIZATION']['ENABLED']:
        return
    
    x_vals = user_wX[:,0]    # 스케일된 나이
    y_vals = user_wX[:,1]    # 스케일된 예산

    # MBTI (4차원) → TSNE(1차원)
    mbti_part = user_wX[:, 2:6]
    tsn = TSNE(
        n_components=1,
        perplexity=30,
        learning_rate=200,
        early_exaggeration=12.0,
        max_iter=800,
        random_state=CONFIG['RANDOM_SEED']
    )
    z_emb = tsn.fit_transform(mbti_part)  # shape (N,1)
    z_vals = z_emb[:,0]  # (N,)

    # 노이즈 (시각적 구분 위한 미세조정)
    np.random.seed(999)
    noise = np.random.normal(loc=0, scale=1.0, size=z_vals.shape)
    z_vals += noise

    labs = np.unique(labels)
    color_map = sns.color_palette("hls", len(labs))

    fig = plt.figure(figsize=(7,5), dpi=CONFIG['VISUALIZATION']['DPI'])
    ax = fig.add_subplot(111, projection='3d')

    for i, lb in enumerate(labs):
        mask = (labels == lb)
        ax.scatter(
            x_vals[mask], 
            y_vals[mask], 
            z_vals[mask],
            color=color_map[i],
            label=f"Cluster {lb}",
            alpha=0.7, 
            s=35
        )

    ax.set_title(title, fontweight='bold')
    ax.set_xlabel("Age (scaled)")
    ax.set_ylabel("Budget (scaled)")
    ax.set_zlabel("MBTI-TSNE(1D)")
    ax.view_init(elev=25, azim=45)
    plt.legend(loc='best', fontsize=9)
    plt.tight_layout()
    plt.savefig(outpath, dpi=CONFIG['VISUALIZATION']['DPI'])
    plt.close()

def plot_3d_couple_custom(cpl_wX, labels, title, outpath):
    """
    x축: TSNE(1d) from (male_age,female_age,age_diff)
    y축: cpl_wX[:,2] => 예산(스케일)
    z축: TSNE(1d) from (marriage_month_sin, mcos, day_sin, dcos, user_dist)
    """
    if not CONFIG['VISUALIZATION']['ENABLED']:
        return

    age_part= cpl_wX[:, [0,1,7]]
    tsn_x= TSNE(n_components=1, perplexity=30, learning_rate=200,
                early_exaggeration=12.0, max_iter=800, random_state=CONFIG['RANDOM_SEED'])
    x_emb= tsn_x.fit_transform(age_part)
    x_vals= x_emb[:,0]

    y_vals= cpl_wX[:,2]

    dd_part= cpl_wX[:, [3,4,5,6,8]]
    tsn_z= TSNE(n_components=1, perplexity=30, learning_rate=200,
                early_exaggeration=12.0, max_iter=800, random_state=CONFIG['RANDOM_SEED'])
    z_emb= tsn_z.fit_transform(dd_part)
    z_vals= z_emb[:,0]

    # 노이즈 (시각적 구분 위한 미세조정)
    np.random.seed(777)
    noise2= np.random.normal(loc=0, scale=1.0, size=z_vals.shape)
    z_vals += noise2

    labs= np.unique(labels)
    color_map= sns.color_palette("hls", len(labs))

    fig= plt.figure(figsize=(7,5), dpi=CONFIG['VISUALIZATION']['DPI'])
    ax= fig.add_subplot(111, projection='3d')

    for i, lb in enumerate(labs):
        ms= (labels==lb)
        ax.scatter(
            x_vals[ms], y_vals[ms], z_vals[ms],
            color=color_map[i], alpha=0.7, s=35,
            label=f"Cluster {lb}"
        )

    ax.set_title(title, fontweight='bold')
    ax.set_xlabel("Age-part TSNE(1D)")
    ax.set_ylabel("Budget (scaled)")
    ax.set_zlabel("Date+Dist TSNE(1D)")
    
    # 예시로 y축 범위를 0~14 정도로 제한
    ax.set_ylim(0, 14)
    
    ax.view_init(elev=25, azim=45)
    plt.legend(loc='best', fontsize=9)
    plt.tight_layout()
    plt.savefig(outpath, dpi=CONFIG['VISUALIZATION']['DPI'])
    plt.close()


# (13) 메인 실행
##############################################################################
@measure_time
def main():
    """
    메인 실행 함수
    1. 데이터 로드 및 전처리
    2. 사용자/커플 클러스터링
    3. 가중치 최적화
    4. 결과 저장 및 시각화
    """
    logger.info("[main] 클러스터링 프로세스 시작")

    # 1) 데이터 로드
    if CONFIG['USE_DUMMY_DATA']:
        couples_json= create_distinct_dummy_couples_json(CONFIG['SAMPLE_SIZE'])
    else:
        fp= CONFIG['RESOURCE_DIR']/ 'input_data.json'
        with open(fp,'r',encoding='utf-8') as ff:
            d= json.load(ff)
        if isinstance(d,list):
            d={'couples':d}
        couples_json= d

    # 2) NumPy 변환
    user_arr, user_ids= convert_users_to_numpy(couples_json)
    cpl_arr, cpl_ids, f_ids, m_ids= convert_couples_to_numpy(couples_json)

    # 3) 사용자 스케일링
    user_scaled, user_scalers= scale_user_features(user_arr)

    # 4) 사용자 K 탐색
    k_range_user= list(CONFIG['USER_K_RANGE'])
    sse_user=[]
    for kk in k_range_user:
        km= KMeans(n_clusters=kk, random_state=CONFIG['RANDOM_SEED'], n_init=10)
        km.fit(user_scaled)
        sse_user.append(km.inertia_)
    elbow_user= find_elbow_point(k_range_user, sse_user)
    logger.info(f"[main] user elbow => {elbow_user}")
    if elbow_user is None:
        elbow_user= k_range_user[-1]

    # 5) 사용자 가중치 최적화
    user_opt= optimize_user_weights(user_scaled, elbow_user)
    user_wX= user_scaled* user_opt['weights']
    user_models= evaluate_models(user_wX, elbow_user)
    best_user_mtype, best_user_data= max(user_models.items(), key=lambda x:x[1]['scores']['composite'])
    user_labels= get_labels_by_model(best_user_data['model'], best_user_mtype, user_wX)
    logger.info(f"[USER] Best Model={best_user_mtype}, scores={best_user_data['scores']}")

    # 6) user dist
    idx_map= {uid:i for i,uid in enumerate(user_ids)}
    dist_list=[]
    for i in range(len(cpl_ids)):
        fi= f_ids[i]
        mi= m_ids[i]
        if fi in idx_map and mi in idx_map:
            dist= np.linalg.norm(user_wX[idx_map[fi]] - user_wX[idx_map[mi]])
        else:
            dist=0.0
        dist_list.append(dist)
    cpl_arr[:,8]= dist_list

    # 7) 커플 스케일링
    cpl_scaled, cpl_scalers= scale_couple_features(cpl_arr)

    # 8) 커플 Elbow
    k_range_couple= list(CONFIG['COUPLE_K_RANGE'])
    sse_couple=[]
    for kc in k_range_couple:
        km2= KMeans(n_clusters=kc, random_state=CONFIG['RANDOM_SEED'], n_init=10)
        km2.fit(cpl_scaled)
        sse_couple.append(km2.inertia_)
    elbow_couple= find_elbow_point(k_range_couple, sse_couple)
    logger.info(f"[main] couple elbow => {elbow_couple}")
    if elbow_couple is None:
        elbow_couple= k_range_couple[-1]

    # 9) 커플 가중치 최적화
    cpl_opt= optimize_couple_weights(cpl_scaled, elbow_couple)
    cpl_wX= cpl_scaled* cpl_opt['weights']
    cpl_models= evaluate_models(cpl_wX, elbow_couple)
    best_cpl_mtype, best_cpl_data= max(cpl_models.items(), key=lambda x:x[1]['scores']['composite'])
    cpl_labels= get_labels_by_model(best_cpl_data['model'], best_cpl_mtype, cpl_wX)
    logger.info(f"[COUPLE] Best Model={best_cpl_mtype}, scores={best_cpl_data['scores']}")

    # 10) 결과 저장
    def _save_scaler_params(ctype, scdict):
        """
        수정된 부분:
        스케일러가 None이 아닐 때만 tmp 딕셔너리를 생성하고,
        최종적으로 dct[k] = tmp 를 통해 결과를 저장하도록 수정.
        """
        dct={}
        for k, sc in scdict.items():
            if sc is None:
                dct[k] = None
            else:
                tmp={}
                if hasattr(sc,'mean_'):
                    tmp['mean_'] = sc.mean_.tolist()
                if hasattr(sc,'var_'):
                    tmp['var_']  = sc.var_.tolist()
                if hasattr(sc,'scale_'):
                    tmp['scale_']= sc.scale_.tolist()
                if hasattr(sc,'min_'):
                    tmp['min_']  = sc.min_.tolist()
                if hasattr(sc,'data_min_'):
                    tmp['data_min_'] = sc.data_min_.tolist()
                if hasattr(sc,'data_max_'):
                    tmp['data_max_'] = sc.data_max_.tolist()
                dct[k] = tmp
        fp= CONFIG['RESOURCE_DIR']/f"{ctype}_scaler_params.json"
        with open(fp,'w',encoding='utf-8') as ff:
            json.dump(dct,ff,indent=2,ensure_ascii=False)

    _save_scaler_params('user', user_scalers)
    _save_scaler_params('couple', cpl_scalers)

    user_assign, couple_assign= assign_cluster_labels(
        user_ids, user_labels, cpl_ids, cpl_labels
    )
    save_cluster_centers('user', best_user_data,
        ['age','budget','mbti_ie','mbti_sn','mbti_tf','mbti_jp'],
        user_wX
    )
    save_cluster_centers('couple', best_cpl_data,
        ['male_age','female_age','budget','marriage_month_sin','marriage_month_cos',
         'marriage_day_sin','marriage_day_cos','age_diff','user_cluster_distance'],
        cpl_wX
    )
    save_assignments('user', user_ids, user_labels, user_wX, idname='userid')
    save_assignments('couple', cpl_ids, cpl_labels, cpl_wX, idname='coupleid')

    # 11) Elbow 그래프
    if CONFIG['PLOT_ELBOW_GRAPHS']:
        fig_u= plot_elbow_graph(k_range_user, sse_user, elbow_user, title='user')
        fu= CONFIG['RESOURCE_DIR']/ "elbow_plot_user.png"
        fig_u.savefig(fu, dpi=120)
        plt.close(fig_u)

        fig_c= plot_elbow_graph(k_range_couple, sse_couple, elbow_couple, title='couple')
        fc= CONFIG['RESOURCE_DIR']/ "elbow_plot_couple.png"
        fig_c.savefig(fc, dpi=120)
        plt.close(fig_c)

    # 12) 맞춤 3D 시각화
    if CONFIG['VISUALIZATION']['ENABLED']:
        # 사용자 x=나이, y=예산, z=MBTI(TSNE)
        u3d_path= CONFIG['RESOURCE_DIR']/ CONFIG['VISUALIZATION']['USER_FEATURES']['filename']
        plot_3d_user_custom(user_wX, user_labels, CONFIG['VISUALIZATION']['USER_FEATURES']['title'], u3d_path)

        # 커플 x=TSNE(남/여나이+나이차), y=예산, z=TSNE(결혼날짜+user_dist)
        c3d_path= CONFIG['RESOURCE_DIR']/ CONFIG['VISUALIZATION']['COUPLE_FEATURES']['filename']
        plot_3d_couple_custom(cpl_wX, cpl_labels, CONFIG['VISUALIZATION']['COUPLE_FEATURES']['title'], c3d_path)

    # 13) 최종 요약
    final_out= {
        'user_results': {
            'best_model_type': best_user_mtype,
            'scores': {k: float(v) for k,v in best_user_data['scores'].items()},
            'optimal_k': int(user_opt['optimal_k'])
        },
        'couple_results': {
            'best_model_type': best_cpl_mtype,
            'scores': {k: float(v) for k,v in best_cpl_data['scores'].items()},
            'optimal_k': int(cpl_opt['optimal_k'])
        }
    }
    save_final_summary(final_out)

    logger.info("[main] 클러스터링 완료")
    return final_out


# (14) 스크립트 직접 실행
##############################################################################
if __name__=="__main__":
    """
    스크립트 직접 실행 시 진입점
    - 리소스 디렉토리 생성
    - 예외 처리 및 종료 코드 반환
    """
    os.makedirs(CONFIG['RESOURCE_DIR'], exist_ok=True)
    try:
        res= main()
        logger.info("[__main__] 완료.")
        sys.exit(0)
    except Exception as e:
        logger.exception(f"[__main__] 예외: {e}")
        sys.exit(1)