package com.ssafy.exhi.domain.recommendation.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.recommendation.converter.RecommendationConverter;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.recommendation.model.entity.*;
import com.ssafy.exhi.domain.recommendation.repository.*;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import com.ssafy.exhi.domain.todo.repository.TodoRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.domain.recommendation.config.RecommendationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 서비스 구현체
 * 1. 사용자/커플의 클러스터 할당 처리
 * 2. 유사 커플 분석을 통한 플랜 추천
 * 3. 추천 결과에 따른 VirginRoad/Plan/Todo 생성 및 업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    // ========== Repository Dependencies ==========
    private final UserRepository userRepository;
    private final UserClusterRepository userClusterRepository;
    private final UserClusterCentersRepository userClusterCentersRepository;
    private final UserClusteringParameterRepository userClusteringParameterRepository;
    private final CoupleRepository coupleRepository;
    private final CoupleClusterRepository coupleClusterRepository;
    private final CoupleClusterCentersRepository coupleClusterCentersRepository;
    private final CoupleClusteringParameterRepository coupleClusteringParameterRepository;
    private final VirginRoadRepository virginRoadRepository;
    private final PlanRepository planRepository;
    private final TodoRepository todoRepository;
    private final DefaultTodoRepository defaultTodoRepository;

    // ========== Cached Parameters ==========
    private volatile UserClusteringParameter userClusteringParam;
    private volatile CoupleClusteringParameter coupleClusteringParam;

    // 동기화를 위한 락 객체
    private final Object lock = new Object();

    private final RecommendationConfig recommendationConfig;

    // ========== Lazy Getter for Clustering Params ==========

    private UserClusteringParameter getUserClusteringParam() {
        if (userClusteringParam == null) {
            synchronized (lock) {
                if (userClusteringParam == null) {
                    userClusteringParam = userClusteringParameterRepository
                            .findFirstByOrderByCreatedAtDesc()
                            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));
                }
            }
        }
        return userClusteringParam;
    }

    private CoupleClusteringParameter getCoupleClusteringParam() {
        if (coupleClusteringParam == null) {
            synchronized (lock) {
                if (coupleClusteringParam == null) {
                    coupleClusteringParam = coupleClusteringParameterRepository
                            .findFirstByOrderByCreatedAtDesc()
                            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));
                }
            }
        }
        return coupleClusteringParam;
    }

    // ========== Public API Methods ==========

    /**
     * 사용자와 파트너를 각각의 클러스터에 할당하고, 커플 클러스터도 함께 할당
     * 1. 사용자 클러스터 할당
     * 2. 파트너 클러스터 할당
     * 3. 커플 클러스터 할당
     */
    @Override
    @Transactional
    public void addUserToCluster(Integer userId) {
        // 트랜잭션 분리 - 조회
        User user = getUserById(userId);
        Couple couple = getCoupleByUserId(userId);

        // 트랜잭션 분리 - 사용자 클러스터 처리
        saveUserCluster(user);
        
        // 트랜잭션 분리 - 파트너 클러스터 처리
        User partnerUser = (user.equals(couple.getMale())) ? couple.getFemale() : couple.getMale();
        saveUserCluster(partnerUser);

        // 트랜잭션 분리 - 커플 클러스터 처리
        saveCoupleCluster(couple);
    }

    // 사용자 클러스터 저장을 위한 별도 트랜잭션
    @Transactional
    protected void saveUserCluster(User user) {
        UserCluster userCluster = findOrCreateUserCluster(user);
        assignUserToCluster(userCluster, user);
        userClusterRepository.save(userCluster);
    }

    // 커플 클러스터 저장을 위한 별도 트랜잭션
    @Transactional
    protected void saveCoupleCluster(Couple couple) {
        CoupleCluster coupleCluster = findOrCreateCoupleCluster(couple);
        calculateCoupleCoordinates(coupleCluster, couple);
        assignCoupleToCluster(coupleCluster, couple);
        coupleClusterRepository.save(coupleCluster);
    }

    /**
     * 사용자에게 맞춤형 Plan을 추천
     * 1. 유사 커플 탐색
     * 2. 유사 커플들의 Plan 분석
     * 3. 분석 결과에 따른 추천 여부 결정
     * 4. VirginRoad/Plan/Todo 생성 또는 업데이트
     */
    @Override
    @Transactional
    public RecommendationResponse.RecommendedPlansDTO getRecommendedPlans(Integer userId) {
        // 조회 로직
        Couple couple = getCoupleByUserId(userId);
        VirginRoad virginRoad = getVirginRoadByCouple(couple);
        
        // 분석 로직 (트랜잭션 없이 메모리에서 처리)
        Map<ServiceType, double[]> planWeights = initializePlanWeights(virginRoad);
        List<Couple> similarCouples = findSimilarCouples(getCoupleCluster(couple));
        analyzeSimilarCouplesPlans(similarCouples, couple, planWeights);

        // 결과 저장을 위한 별도 트랜잭션 호출
        VirginRoad finalVR = saveRecommendationResults(couple, virginRoad, planWeights);

        return RecommendationConverter.toRecommendedPlansDTO(finalVR, planWeights);
    }

    // VirginRoad 저장을 위한 별도 트랜잭션
    @Transactional
    protected VirginRoad saveRecommendationResults(Couple couple, 
                                                  VirginRoad virginRoad,
                                                  Map<ServiceType, double[]> planWeights) {
        return createRecommendVirginRoad(couple, virginRoad, planWeights);
    }

    // 조회 전용 메서드들
    @Transactional(readOnly = true)
    protected User getUserById(Integer userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    protected Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));
    }

    // ========== Private: VirginRoad & Plan 생성/업데이트 로직 ==========

    /**
     * 추천 가중치에 따라 VirginRoad와 하위 Plan, Todo를 생성 또는 갱신
     * - VirginRoad가 없는 경우 새로 생성
     * - 각 ServiceType별 Plan 생성
     * - DefaultTodo 기반으로 Todo 생성
     * - planWeights에 따라 Plan visibility 설정
     */
    private VirginRoad createRecommendVirginRoad(Couple couple,
                                                 VirginRoad virginRoad,
                                                 Map<ServiceType, double[]> planWeights) {
        // (1) 아직 VirginRoad가 없다면 생성
        if (virginRoad == null) {
            virginRoad = VirginRoad.builder()
                    .couple(couple)
                    .virginRoadStatus(PlanStatus.BEFORE_START)
                    .plans(new ArrayList<>())
                    .build();

            virginRoad = virginRoadRepository.save(virginRoad);

            // DefaultTodo 데이터를 ServiceType별로 조회
            Map<ServiceType, List<DefaultTodo>> defaultTodoMap = getAllDefaultTodosInMap();

            // 모든 ServiceType에 대해 Plan 생성
            for (ServiceType serviceType : ServiceType.values()) {
                Plan plan = Plan.builder()
                        .virginRoad(virginRoad)
                        .serviceType(serviceType)
                        .visible(false)
                        .planStatus(PlanStatus.BEFORE_START)
                        .todos(new ArrayList<>())
                        .build();

                plan = planRepository.save(plan);

                // DefaultTodo -> Todo 변환하여 연결
                if (defaultTodoMap.containsKey(serviceType)) {
                    for (DefaultTodo defaultTodo : defaultTodoMap.get(serviceType)) {
                        Todo todo = Todo.builder()
                                .plan(plan)
                                .content(defaultTodo.getContent())
                                .isCompleted(false)
                                .todoType(TodoType.valueOf(defaultTodo.getGender().name()))
                                .build();
                        todoRepository.save(todo);
                        plan.getTodos().add(todo);
                    }
                }

                virginRoad.getPlans().add(plan);
            }
        }

        // (2) planWeights 기준으로 visibility 설정
        for (Plan plan : virginRoad.getPlans()) {
            double[] weights = planWeights.getOrDefault(plan.getServiceType(), new double[]{0, 0});
            // [0] == 1이면 추천 -> visible = true
            plan.setVisible(weights[0] == 1);
        }

        return virginRoadRepository.saveAndFlush(virginRoad);
    }

    /**
     * 모든 DefaultTodo를 조회해 ServiceType별로 묶어 반환
     */
    private Map<ServiceType, List<DefaultTodo>> getAllDefaultTodosInMap() {
        List<DefaultTodo> allDefaultTodos = defaultTodoRepository.findAll();
        Map<ServiceType, List<DefaultTodo>> map = new HashMap<>();

        for (DefaultTodo defaultTodo : allDefaultTodos) {
            map.computeIfAbsent(defaultTodo.getServiceType(), k -> new ArrayList<>()).add(defaultTodo);
        }
        return map;
    }

    // ========== Private: 클러스터 할당 로직 ==========

    /**
     * 사용자를 위한 클러스터를 조회하거나 없으면 새로 생성
     */
    private UserCluster findOrCreateUserCluster(User user) {
        return userClusterRepository.findByUser(user)
                .orElse(UserCluster.builder().user(user).build());
    }

    /**
     * 커플을 위한 클러스터를 조회하거나 없으면 새로 생성
     */
    private CoupleCluster findOrCreateCoupleCluster(Couple couple) {
        return coupleClusterRepository.findByCouple(couple)
                .orElse(CoupleCluster.builder().couple(couple).build());
    }

    /**
     * 사용자 좌표를 계산한 뒤, 최근 클러스터 중심점들 중 가장 가까운 clusterId로 할당
     */
    private void assignUserToCluster(UserCluster userCluster, User user) {
        // 최신 userClusterCenters 조회
        UserClusterCenters centers = userClusterCentersRepository.findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS));

        // 사용자 좌표화
        calculateUserCoordinates(userCluster, user);

        // 최단거리 탐색
        double minDistance = Double.MAX_VALUE;
        int nearestClusterId = -1;

        for (UserClusterCenter center : centers.getUserClusterCenterList()) {
            // (A) 사용자와 해당 center 사이 유클리드 거리
            double distance = calculateEuclideanDistance(
                new double[]{
                    userCluster.getAgeCoordinate(),
                    userCluster.getBudgetCoordinate(),
                    userCluster.getMbtiIeCoordinate(),
                    userCluster.getMbtiSnCoordinate(),
                    userCluster.getMbtiTfCoordinate(),
                    userCluster.getMbtiJpCoordinate()
                },
                new double[]{
                    center.getAgeCoordinate(),
                    center.getBudgetCoordinate(),
                    center.getMbtiIeCoordinate(),
                    center.getMbtiSnCoordinate(),
                    center.getMbtiTfCoordinate(),
                    center.getMbtiJpCoordinate()
                }
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearestClusterId = center.getClusterId();
            }
        }

        if (nearestClusterId == -1) {
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS);
        }

        userCluster.setClusterId(nearestClusterId);
        userCluster.setUser(user);
    }

    /**
     * 커플 좌표를 계산한 뒤, 최근 클러스터 중심점들 중 가장 가까운 clusterId로 할당
     */
    private void assignCoupleToCluster(CoupleCluster coupleCluster, Couple couple) {
        // 최신 coupleClusterCenters 조회
        CoupleClusterCenters centers = coupleClusterCentersRepository.findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS));

        double minDistance = Double.MAX_VALUE;
        int nearestClusterId = -1;

        for (CoupleClusterCenter center : centers.getCoupleClusterCenterList()) {
            // (B) 커플과 해당 center 사이 유클리드 거리
            double distance = calculateEuclideanDistance(
                new double[]{
                    coupleCluster.getMaleAgeCoordinate(),
                    coupleCluster.getFemaleAgeCoordinate(),
                    coupleCluster.getAgeDiffCoordinate(),
                    coupleCluster.getBudgetCoordinate(),
                    coupleCluster.getMarriageMonthSinCoordinate(),
                    coupleCluster.getMarriageMonthCosCoordinate(),
                    coupleCluster.getMarriageDaySinCoordinate(),
                    coupleCluster.getMarriageDayCosCoordinate(),
                    coupleCluster.getUserClusterDistanceCoordinate()
                },
                new double[]{
                    center.getMaleAgeCoordinate(),
                    center.getFemaleAgeCoordinate(),
                    center.getAgeDiffCoordinate(),
                    center.getBudgetCoordinate(),
                    center.getMarriageMonthSinCoordinate(),
                    center.getMarriageMonthCosCoordinate(),
                    center.getMarriageDaySinCoordinate(),
                    center.getMarriageDayCosCoordinate(),
                    center.getUserClusterDistanceCoordinate()
                }
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearestClusterId = center.getClusterId();
            }
        }

        if (nearestClusterId == -1) {
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS);
        }

        coupleCluster.setClusterId(nearestClusterId);
        coupleCluster.setCouple(couple);
    }

    // ========== Private: 좌표 계산/스케일링 로직 ==========

    /**
     * 유클리드 거리 계산
     */
    private double calculateEuclideanDistance(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        double sumSquared = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sumSquared += diff * diff;
        }
        return Math.sqrt(sumSquared);
    }

    /**
     * 사용자 좌표를 스케일링해서 UserCluster 에 세팅
     */
    private void calculateUserCoordinates(UserCluster userCluster, User user) {
        // 사용자 상세
        UserDetail detail = user.getUserDetail();
        if (detail == null) {
            throw new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND);
        }

        UserClusteringParameter param = getUserClusteringParam();

        // 나이(표준 스케일)
        userCluster.setAgeCoordinate(
            scaleStandard(detail.getAge(), param.getAgeMean(), param.getAgeScale())
        );

        // 예산(MinMax 스케일)
        int budget = getCoupleByUserId(user.getId()).getBudget();
        userCluster.setBudgetCoordinate(
            scaleMinMaxWithRange(
                budget,
                param.getBudgetDataMin(),
                param.getBudgetDataMax(),
                param.getBudgetMin(),
                param.getBudgetScale()
            )
        );

        // MBTI
        String mbti = detail.getMbti();
        if (mbti != null && mbti.length() == 4) {
            userCluster.setMbtiIeCoordinate(mbti.charAt(0) == 'E' ? 1.0 : 0.0);
            userCluster.setMbtiSnCoordinate(mbti.charAt(1) == 'S' ? 1.0 : 0.0);
            userCluster.setMbtiTfCoordinate(mbti.charAt(2) == 'T' ? 1.0 : 0.0);
            userCluster.setMbtiJpCoordinate(mbti.charAt(3) == 'J' ? 1.0 : 0.0);
        } else {
            // MBTI 정보 없으면 -1
            userCluster.setMbtiIeCoordinate(-1.0);
            userCluster.setMbtiSnCoordinate(-1.0);
            userCluster.setMbtiTfCoordinate(-1.0);
            userCluster.setMbtiJpCoordinate(-1.0);
        }
    }

    /**
     * 커플 좌표를 스케일링해서 CoupleCluster 에 세팅
     */
    private void calculateCoupleCoordinates(CoupleCluster coupleCluster, Couple couple) {
        CoupleClusteringParameter param = getCoupleClusteringParam();

        // 남편/아내 나이를 표준 스케일
        int maleAge = couple.getMale().getUserDetail().getAge();
        int femaleAge = couple.getFemale().getUserDetail().getAge();

        coupleCluster.setMaleAgeCoordinate(
            scaleStandard(maleAge, param.getMaleAgeMean(), param.getMaleAgeScale())
        );
        coupleCluster.setFemaleAgeCoordinate(
            scaleStandard(femaleAge, param.getFemaleAgeMean(), param.getFemaleAgeScale())
        );

        // 나이 차이
        int ageDiff = maleAge - femaleAge;
        coupleCluster.setAgeDiffCoordinate(
            scaleMinMaxWithRange(
                ageDiff,
                param.getAgeDiffDataMin(),
                param.getAgeDiffDataMax(),
                param.getAgeDiffMin(),
                param.getAgeDiffScale()
            )
        );

        // 예산
        coupleCluster.setBudgetCoordinate(
            scaleMinMaxWithRange(
                couple.getBudget(),
                param.getBudgetDataMin(),
                param.getBudgetDataMax(),
                param.getBudgetMin(),
                param.getBudgetScale()
            )
        );

        // 결혼 날짜 -> sin/cos
        LocalDate md = couple.getMarriageDate();
        int month = md.getMonthValue();
        int day = md.getDayOfMonth();

        coupleCluster.setMarriageMonthSinCoordinate(
            scaleMinMaxWithRange(
                Math.sin(2 * Math.PI * month / 12.0),
                param.getMarriageMonthSinDataMin(),
                param.getMarriageMonthSinDataMax(),
                param.getMarriageMonthSinMin(),
                param.getMarriageMonthSinScale()
            )
        );
        coupleCluster.setMarriageMonthCosCoordinate(
            scaleMinMaxWithRange(
                Math.cos(2 * Math.PI * month / 12.0),
                param.getMarriageMonthCosDataMin(),
                param.getMarriageMonthCosDataMax(),
                param.getMarriageMonthCosMin(),
                param.getMarriageMonthCosScale()
            )
        );

        coupleCluster.setMarriageDaySinCoordinate(
            scaleMinMaxWithRange(
                Math.sin(2 * Math.PI * day / 31.0),
                param.getMarriageDaySinDataMin(),
                param.getMarriageDaySinDataMax(),
                param.getMarriageDaySinMin(),
                param.getMarriageDaySinScale()
            )
        );
        coupleCluster.setMarriageDayCosCoordinate(
            scaleMinMaxWithRange(
                Math.cos(2 * Math.PI * day / 31.0),
                param.getMarriageDayCosDataMin(),
                param.getMarriageDayCosDataMax(),
                param.getMarriageDayCosMin(),
                param.getMarriageDayCosScale()
            )
        );

        // 유저 클러스터 거리
        double userClusterDistance = calculateUserClusterDistance(couple);
        coupleCluster.setUserClusterDistanceCoordinate(userClusterDistance);
    }

    /**
     * 남편/아내 각각의 클러스터 거리 평균을 구해 MinMax 스케일링
     */
    private double calculateUserClusterDistance(Couple couple) {
        UserCluster maleCluster = getUserCluster(couple.getMale());
        UserCluster femaleCluster = getUserCluster(couple.getFemale());

        UserClusterCenters centers = userClusterCentersRepository
                .findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        // 각자 클러스터 중심점
        UserClusterCenter maleCenter = centers.getUserClusterCenterList().stream()
                .filter(c -> Objects.equals(c.getClusterId(), maleCluster.getClusterId()))
                .findFirst()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        UserClusterCenter femaleCenter = centers.getUserClusterCenterList().stream()
                .filter(c -> Objects.equals(c.getClusterId(), femaleCluster.getClusterId()))
                .findFirst()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        // 실제 거리 계산
        double maleDistance = calculateUserDistance(couple.getMale(), maleCenter, couple);
        double femaleDistance = calculateUserDistance(couple.getFemale(), femaleCenter, couple);
        double rawDistance = (maleDistance + femaleDistance) / 2.0;

        // MinMax 스케일
        return scaleMinMaxWithRange(
            rawDistance,
            getCoupleClusteringParam().getUserClusterDistDataMin(),
            getCoupleClusteringParam().getUserClusterDistDataMax(),
            getCoupleClusteringParam().getUserClusterDistMin(),
            getCoupleClusteringParam().getUserClusterDistScale()
        );
    }

    /**
     * 남/여 각각의 좌표값과 center를 비교해 거리 계산 (가중치 포함)
     */
    private double calculateUserDistance(User user, UserClusterCenter center, Couple couple) {
        UserCluster userCluster = getUserCluster(user);

        // 나이 차
        double ageDiff = (userCluster.getAgeCoordinate() - center.getAgeCoordinate())
                * getUserClusteringParam().getAgeWeight();
        double ageDistSq = ageDiff * ageDiff;

        // MBTI (IE, SN, TF, JP)
        double ieDiff = (userCluster.getMbtiIeCoordinate() - center.getMbtiIeCoordinate())
                * getUserClusteringParam().getMbtiIeWeight();
        double snDiff = (userCluster.getMbtiSnCoordinate() - center.getMbtiSnCoordinate())
                * getUserClusteringParam().getMbtiSnWeight();
        double tfDiff = (userCluster.getMbtiTfCoordinate() - center.getMbtiTfCoordinate())
                * getUserClusteringParam().getMbtiTfWeight();
        double jpDiff = (userCluster.getMbtiJpCoordinate() - center.getMbtiJpCoordinate())
                * getUserClusteringParam().getMbtiJpWeight();
        double mbtiDistSq = ieDiff * ieDiff + snDiff * snDiff + tfDiff * tfDiff + jpDiff * jpDiff;

        // 예산 차
        double budgetDiff = (userCluster.getBudgetCoordinate() - center.getBudgetCoordinate())
                * getUserClusteringParam().getBudgetWeight();
        double budgetDistSq = budgetDiff * budgetDiff;

        // 최종 거리
        return Math.sqrt(ageDistSq + mbtiDistSq + budgetDistSq);
    }

    // ========== Private: 추천 로직 (유사 커플 플랜 분석) ==========

    /**
     * 유사 커플 찾기 (같은 clusterId + 본인 제외)
     * DB에서 직접 100개까지 무작위 선택
     */
    private List<Couple> findSimilarCouples(CoupleCluster coupleCluster) {
        return coupleRepository.findRandomSimilarCouples(
            coupleCluster.getClusterId(), 
            coupleCluster.getCouple().getId()
        );
    }

    /**
     * VirginRoad가 있으면 이미 시작된 Plan은 [0]=1, 없으면 전부 [0]=0으로 초기화
     */
    private Map<ServiceType, double[]> initializePlanWeights(VirginRoad virginRoad) {
        Map<ServiceType, double[]> planWeights = new HashMap<>();
        for (ServiceType type : ServiceType.values()) {
            double[] arr = new double[2];  // [추천여부, 점수합]
            if (virginRoad != null) {
                Plan plan = virginRoad.findPlanByServiceType(type);
                if (plan != null) {
                    arr[0] = (plan.getPlanStatus() == PlanStatus.BEFORE_START) ? 0 : 1;
                }
            }
            planWeights.put(type, arr);
        }
        return planWeights;
    }

    /**
     * 유사 커플들의 Plan을 분석하여 추천 가중치 계산
     * 1. 코사인 유사도와 거리 기반 유사도를 결합하여 커플 간 유사도 계산
     * 2. Plan 상태와 TimeCapsule 데이터를 고려한 점수 계산
     * 3. 유사도와 점수를 결합하여 최종 가중치 누적
     */
    private void analyzeSimilarCouplesPlans(List<Couple> similarCouples,
                                            Couple currentCouple,
                                            Map<ServiceType, double[]> planWeights) {
        if (similarCouples.isEmpty()) return;

        // 현재 커플 정보
        CoupleCluster currentCluster = getCoupleCluster(currentCouple);
        double[] currentVector = calculateCoupleVector(currentCluster);

        Map<ServiceType, Integer> totalPlans = new HashMap<>();
        Map<ServiceType, Double> totalSimilarities = new HashMap<>();
        for (ServiceType type : planWeights.keySet()) {
            totalPlans.put(type, 0);
            totalSimilarities.put(type, 0.0);
        }

        for (Couple sc : similarCouples) {
            if (sc.equals(currentCouple)) continue;

            try {
                CoupleCluster scCluster = getCoupleCluster(sc);
                double[] scVector = calculateCoupleVector(scCluster);

                // Config의 메서드 사용 (currentVector는 위에서 선언한 것 사용)
                double finalSimilarity = recommendationConfig.computeFinalSimilarity(
                    currentVector, scVector
                );

                VirginRoad scVR = getVirginRoadByCouple(sc);
                for (Plan plan : scVR.getPlans()) {
                    ServiceType st = plan.getServiceType();
                    if (!planWeights.containsKey(st) || planWeights.get(st)[0] == 1) {
                        continue;
                    }

                    totalPlans.put(st, totalPlans.get(st) + 1);
                    totalSimilarities.put(st, totalSimilarities.get(st) + finalSimilarity);
                    double planScore = calculatePlanScore(plan, scVR.getVirginRoadStatus());
                    planWeights.get(st)[1] += planScore;
                }

            } catch (ExceptionHandler e) {
                log.warn("Error analyzing couple {}: {}", sc.getId(), e.getMessage());
            }
        }

        determineRecommendations(planWeights, totalPlans, totalSimilarities);
    }

    /**
     * 커플 클러스터 -> 실수 벡터로 변환
     */
    private double[] calculateCoupleVector(CoupleCluster cc) {
        return new double[]{
                cc.getMaleAgeCoordinate(),
                cc.getFemaleAgeCoordinate(),
                cc.getAgeDiffCoordinate(),
                cc.getBudgetCoordinate(),
                cc.getMarriageMonthSinCoordinate(),
                cc.getMarriageMonthCosCoordinate(),
                cc.getMarriageDaySinCoordinate(),
                cc.getMarriageDayCosCoordinate(),
                cc.getUserClusterDistanceCoordinate()
        };
    }

    /**
     * 플랜 상태와 타임캡슐 여부로 플랜 점수 계산
     */
    private double calculatePlanScore(Plan plan, PlanStatus vrStatus) {
        double score = 0.0;
        switch (plan.getPlanStatus()) {
            case FINISHED -> {
                score = 2.0;
                if (!plan.getTimeCapsules().isEmpty()) {
                    score += calculateTimeCapsuleScore(plan.getTimeCapsules());
                }
                if (vrStatus == PlanStatus.FINISHED) {
                    score *= 1.5;
                }
            }
            case IN_PROGRESS -> score = 1.0;
            case BEFORE_START -> score = 0.0;
        }
        return score;
    }

    /**
     * 타임캡슐 점수 (긍정2점, 부정-1점) -> [0~1] 범위 정규화
     */
    private double calculateTimeCapsuleScore(List<TimeCapsule> timeCapsules) {
        if (timeCapsules.isEmpty()) return 0.0;
        int weight = timeCapsules.stream()
                // isPlanGrade() == true이면 부정(-1), false이면 긍정(2)라고 가정한 로직
                .mapToInt(tc -> tc.isPlanGrade() ? -1 : 2)
                .sum();
        double raw = (double) weight / 5.0; // 임의로 5.0 분모
        return Math.min(Math.max(raw, 0.0), 1.0);
    }

    /**
     * 누적된 점수를 토대로 최종 추천 여부 결정
     */
    private void determineRecommendations(Map<ServiceType, double[]> planWeights,
                                          Map<ServiceType, Integer> totalPlans,
                                          Map<ServiceType, Double> totalSimilarities) {
        double recommendationThreshold = recommendationConfig.getRecommendationThreshold();

        for (Map.Entry<ServiceType, double[]> entry : planWeights.entrySet()) {
            ServiceType serviceType = entry.getKey();
            double[] weights = entry.getValue();

            if (weights[0] == 1 || totalPlans.get(serviceType) == 0) continue;

            // 평균 점수 계산 (누적 점수 / 플랜 수)
            double avgScore = weights[1] / totalPlans.get(serviceType);
            
            // 평균 유사도 계산
            double avgSimilarity = totalSimilarities.get(serviceType) / totalPlans.get(serviceType);

            // 최종 점수 계산 방식 변경: 
            // 1. avgScore를 100점 만점으로 스케일링 (finishedPlanBaseScore가 만점)
            // 2. 유사도를 가중치로 적용 (0.7~1.0 범위)
            double normalizedScore = (avgScore * 100.0) / recommendationConfig.getFinishedPlanBaseScore();
            double finalScore = normalizedScore * avgSimilarity;

            log.info(String.format(
                    "[Recommendation Score] " +
                            "ServiceType: %s, " +
                            "Score: %.2f%%, " +
                            "AvgScore: %.2f, " +
                            "AvgSimilarity: %.2f, " +
                            "Count: %d",
                    serviceType,
                    finalScore,
                    avgScore,
                    avgSimilarity,
                    totalPlans.get(serviceType)));
            if (finalScore >= recommendationThreshold) {
                weights[0] = 1;
                log.info(String.format(
                        "[Recommendation Result] " +
                        "%s" +
                        " -> RECOMMENDED " +
                        "%.2f%%",
                        serviceType,
                        finalScore));
            } else {
                log.info(String.format(
                "[Recommendation Result] " +
                        "%s" +
                        " -> NOT RECOMMENDED " +
                        "%.2f%%",
                        serviceType,
                        finalScore));
            }
        }
    }

    // ========== Private: 엔티티/리포지토리 조회 ==========

    private UserCluster getUserCluster(User user) {
        return userClusterRepository.findByUser(user)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_CLUSTER_NOT_FOUND));
    }

    private CoupleCluster getCoupleCluster(Couple couple) {
        return coupleClusterRepository.findByCouple(couple)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_CLUSTER_NOT_FOUND));
    }

    private VirginRoad getVirginRoadByCouple(Couple couple) {
        return virginRoadRepository.getVirginRoadByCouple(couple).orElse(null);
    }

    // ========== Private: 스케일링 유틸 ==========

    /**
     * 표준 스케일링 (z-score): (x - mean) / scale
     */
    private double scaleStandard(double value, double mean, double scale) {
        return (value - mean) / scale;
    }

    /**
     * MinMax 스케일링 + (targetMin, targetMin + targetScale) 범위로 매핑
     */
    private double scaleMinMaxWithRange(double value,
                                        double dataMin,
                                        double dataMax,
                                        double targetMin,
                                        double targetScale) {
        if (dataMax == dataMin) {
            // 혹시 모를 0분모 방지
            return targetMin;
        }
        double scaled01 = (value - dataMin) / (dataMax - dataMin);
        return targetMin + (scaled01 * targetScale);
    }
}
