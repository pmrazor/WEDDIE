package com.ssafy.exhi.domain.recommendation.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.recommendation.config.RecommendationConfig;
import com.ssafy.exhi.domain.recommendation.converter.RecommendationConverter;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.recommendation.model.dto.RoadPlanCapsuleDTO;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleCluster;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleClusterCenter;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleClusterCenters;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleClusteringParameter;
import com.ssafy.exhi.domain.recommendation.model.entity.UserCluster;
import com.ssafy.exhi.domain.recommendation.model.entity.UserClusterCenter;
import com.ssafy.exhi.domain.recommendation.model.entity.UserClusterCenters;
import com.ssafy.exhi.domain.recommendation.model.entity.UserClusteringParameter;
import com.ssafy.exhi.domain.recommendation.repository.CoupleClusterCentersRepository;
import com.ssafy.exhi.domain.recommendation.repository.CoupleClusterRepository;
import com.ssafy.exhi.domain.recommendation.repository.CoupleClusteringParameterRepository;
import com.ssafy.exhi.domain.recommendation.repository.UserClusterCentersRepository;
import com.ssafy.exhi.domain.recommendation.repository.UserClusterRepository;
import com.ssafy.exhi.domain.recommendation.repository.UserClusteringParameterRepository;
import com.ssafy.exhi.domain.todo.config.TodoInitializer;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.util.ServiceTypeScheduler;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추천 서비스 구현체
 * - N+1 문제를 피하기 위해 DTO를 활용하여 필요한 데이터를 한 번에 조회
 * - 점수 계산 로직은 RecommendationConfig에 위임
 * - 모든 로직에서 DB 조회, 클러스터링, 추천점수 계산을 통합적으로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    // ========== 레포지토리 의존성 ==========
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
    private final ServiceTypeScheduler serviceTypeScheduler;

    private final TodoInitializer todoInitializer;

    // ========== 캐시된 파라미터 ==========
    private volatile UserClusteringParameter userClusteringParam;
    private volatile CoupleClusteringParameter coupleClusteringParam;
    private final Object lock = new Object(); // 동기화용

    // ========== 추천 설정(점수 로직) ==========
    private final RecommendationConfig recommendationConfig;

    // ========== 클러스터링 파라미터 지연 조회 ==========

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

    // ========== 1) 클러스터 할당 및 업데이트 ==========

    @Override
    @Transactional
    public void addUserToCluster(Integer userId) {
        // 사용자 및 커플 조회
        User user = getUserById(userId);
        Couple couple = getCoupleByUserId(userId);

        // (추가) 사전 검증 로직
        validateUserAndCouple(user, couple);

        // 사용자 클러스터 업데이트
        saveUserCluster(user);

        // 파트너도 동일 처리
        User partnerUser = user.equals(couple.getMale()) ? couple.getFemale() : couple.getMale();
        saveUserCluster(partnerUser);

        // 커플 클러스터 업데이트
        saveCoupleCluster(couple);
    }

    @Transactional
    protected void saveUserCluster(User user) {
        UserCluster userCluster = findOrCreateUserCluster(user);
        assignUserToCluster(userCluster, user);
        userClusterRepository.save(userCluster);
    }

    @Transactional
    protected void saveCoupleCluster(Couple couple) {
        CoupleCluster coupleCluster = findOrCreateCoupleCluster(couple);
        calculateCoupleCoordinates(coupleCluster, couple);
        assignCoupleToCluster(coupleCluster, couple);
        coupleClusterRepository.save(coupleCluster);
    }

    // ========== 2) 최종 추천 로직: 사용자에게 개인화 추천 ==========

    @Override
    @Transactional
    public RecommendationResponse.RecommendedPlansDTO getRecommendedPlans(Integer userId) {
        // 커플, 버진로드 조회
        Couple couple = getCoupleByUserId(userId);
        VirginRoad virginRoad = getVirginRoadByCouple(couple);

        // (추가) 사전 검증 로직
        User user = getUserById(userId);
        validateUserAndCouple(user, couple);

        // ServiceType별 [추천여부, 점수합] 초기화
        Map<ServiceType, double[]> planWeights = initializePlanWeights(virginRoad);

        // 유사 커플 찾기
        List<Couple> similarCouples = findSimilarCouples(getCoupleCluster(couple));

        // 유사 커플들의 ID 모으기
        List<Integer> coupleIds = similarCouples.stream()
            .map(Couple::getId)
            .collect(Collectors.toList());

        // 하나의 쿼리로 RoadPlanCapsuleDTO 전부 가져오기
        if (!coupleIds.isEmpty()) {
            List<RoadPlanCapsuleDTO> allRoadPlanData = virginRoadRepository.findAllMinimalDataByCoupleIds(coupleIds);

            // 커플ID → 해당 DTO들 매핑
            Map<Integer, List<RoadPlanCapsuleDTO>> roadPlanMapByCoupleId = allRoadPlanData.stream()
                .collect(Collectors.groupingBy(RoadPlanCapsuleDTO::getCoupleId));

            // 유사 커플의 플랜 분석
            analyzeSimilarCouplesPlans(similarCouples, couple, planWeights, roadPlanMapByCoupleId);
        }

        // 추천 결과(Plan visibility 설정) 저장
        VirginRoad finalVR = saveRecommendationResults(couple, virginRoad, planWeights);

        // 결과를 DTO로 변환하여 반환
        return RecommendationConverter.toRecommendedPlansDTO(finalVR, planWeights);
    }

    @Transactional
    protected VirginRoad saveRecommendationResults(Couple couple,
                                                   VirginRoad virginRoad,
                                                   Map<ServiceType, double[]> planWeights) {
        return createRecommendVirginRoad(couple, virginRoad, planWeights);
    }

    // ========== (추가) 유저·커플 필수 정보 검증 메서드 ==========

    /**
     * 사용자(User)와 커플(Couple)에 반드시 필요한 정보(나이, 예산, 결혼예정일)에 대해
     * 값이 유효한지 검사합니다.
     * - user.userDetail.age > 0
     * - couple.budget > 0
     * - couple.marriageDate != null
     * 위 세 항목 중 하나라도 유효하지 않으면 예외를 발생시켜 로직을 중단합니다.
     */
    private void validateUserAndCouple(User user, Couple couple) {
        // 1) 사용자 나이 검증
        UserDetail detail = user.getUserDetail();
        if (detail == null || detail.getAge() == null || detail.getAge() <= 0) {
            throw new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND);
        }

        // 2) 커플 예산 검증
        if (couple.getBudget() == null || couple.getBudget() <= 0) {
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY);
        }

        // 3) 커플 결혼예정일 검증
        LocalDate marriageDate = couple.getMarriageDate();
        if (marriageDate == null) {
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY);
        }
    }

    // ========== 3) VirginRoad / Plan 생성 및 갱신 로직 ==========

    @Transactional
    protected VirginRoad createRecommendVirginRoad(Couple couple,
                                                   VirginRoad virginRoad,
                                                   Map<ServiceType, double[]> planWeights) {

        // (1) virginRoad가 없으면 새로 생성 + 기본 플랜/투두 등록
        if (virginRoad == null) {
            virginRoad = VirginRoad.builder()
                .couple(couple)
                .virginRoadStatus(PlanStatus.BEFORE_START)
                .plans(new ArrayList<>())
                .build();

            // 먼저 VirginRoad를 저장
            virginRoad = virginRoadRepository.save(virginRoad);

            List<Plan> plans = new ArrayList<>();
            for (ServiceType serviceType : ServiceType.values()) {
                Plan plan = Plan.createDefault(serviceType, virginRoad);
                // Plan을 저장
                plan = planRepository.save(plan);
                todoInitializer.initializeTodosForPlan(plan);
                plans.add(plan);
            }

            List<Plan> updatePlans = setPlanDefaultDates(plans, couple);

            virginRoad.addPlans(updatePlans);

            virginRoad = virginRoadRepository.save(virginRoad);
        }

        // (2) planWeights를 바탕으로 plan.visibility 설정
        for (Plan plan : virginRoad.getPlans()) {
            double[] weights = planWeights.getOrDefault(plan.getServiceType(), new double[]{0, 0});
            plan.setVisible(weights[0] == 1); // 추천 여부(1)이면 visible = true
        }

        return virginRoadRepository.saveAndFlush(virginRoad);
    }

    private List<Plan> setPlanDefaultDates(List<Plan> Plans, Couple couple) {

        LocalDate marriageDate = couple.getMarriageDate();
        LocalDate today = LocalDate.now();
        long totalDays = ChronoUnit.DAYS.between(today, marriageDate);

        Map<ServiceType, Double> scheduleDays = serviceTypeScheduler.getServiceTypeSchedule();
        List<Plan> updatedPlans = new ArrayList<>();

        for (Plan cur : Plans) {
            double percentage = scheduleDays.getOrDefault(cur.getServiceType(), 0.3);

            long daysBeforeWedding = (long) (totalDays * percentage);
            LocalDate planDate = marriageDate.minusDays(daysBeforeWedding);
            cur.setPlanTime(planDate.atStartOfDay());

            cur.calculateStartAndEndDates();

            updatedPlans.add(cur);
        }

        return updatedPlans;
    }

    // ========== 4) 클러스터 할당 로직 ==========

    private UserCluster findOrCreateUserCluster(User user) {
        return userClusterRepository.findByUser(user)
            .orElse(UserCluster.builder().user(user).build());
    }

    private CoupleCluster findOrCreateCoupleCluster(Couple couple) {
        return coupleClusterRepository.findByCouple(couple)
            .orElse(CoupleCluster.builder().couple(couple).build());
    }

    private void assignUserToCluster(UserCluster userCluster, User user) {
        UserClusterCenters centers = userClusterCentersRepository.findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS));

        // 사용자 좌표 계산
        calculateUserCoordinates(userCluster, user);

        // 가장 가까운 클러스터 검색
        double minDistance = Double.MAX_VALUE;
        int nearestClusterId = -1;

        for (UserClusterCenter center : centers.getUserClusterCenterList()) {
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

    private void assignCoupleToCluster(CoupleCluster coupleCluster, Couple couple) {
        CoupleClusterCenters centers = coupleClusterCentersRepository.findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS));

        double minDistance = Double.MAX_VALUE;
        int nearestClusterId = -1;

        for (CoupleClusterCenter center : centers.getCoupleClusterCenterList()) {
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

    // ========== 5) 좌표 계산 및 스케일링 로직 ==========

    private double calculateEuclideanDistance(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("벡터 길이가 일치하지 않습니다.");
        }
        double sumSquared = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sumSquared += diff * diff;
        }
        return Math.sqrt(sumSquared);
    }

    private void calculateUserCoordinates(UserCluster userCluster, User user) {
        UserDetail detail = user.getUserDetail();
        if (detail == null) {
            throw new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND);
        }
        UserClusteringParameter param = getUserClusteringParam();

        // 나이 (z-score)
        userCluster.setAgeCoordinate(
            scaleStandard(detail.getAge(), param.getAgeMean(), param.getAgeScale())
        );

        // 예산 (Min-Max)
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
            userCluster.setMbtiIeCoordinate(-1.0);
            userCluster.setMbtiSnCoordinate(-1.0);
            userCluster.setMbtiTfCoordinate(-1.0);
            userCluster.setMbtiJpCoordinate(-1.0);
        }
    }

    private void calculateCoupleCoordinates(CoupleCluster coupleCluster, Couple couple) {
        CoupleClusteringParameter param = getCoupleClusteringParam();

        // 남편, 아내 나이
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

        // 커플 예산
        coupleCluster.setBudgetCoordinate(
            scaleMinMaxWithRange(
                couple.getBudget(),
                param.getBudgetDataMin(),
                param.getBudgetDataMax(),
                param.getBudgetMin(),
                param.getBudgetScale()
            )
        );

        // 결혼 날짜 → sin/cos
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

    private double calculateUserClusterDistance(Couple couple) {
        UserCluster maleCluster = getUserCluster(couple.getMale());
        UserCluster femaleCluster = getUserCluster(couple.getFemale());

        UserClusterCenters centers = userClusterCentersRepository
            .findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        UserClusterCenter maleCenter = centers.getUserClusterCenterList().stream()
            .filter(c -> Objects.equals(c.getClusterId(), maleCluster.getClusterId()))
            .findFirst()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        UserClusterCenter femaleCenter = centers.getUserClusterCenterList().stream()
            .filter(c -> Objects.equals(c.getClusterId(), femaleCluster.getClusterId()))
            .findFirst()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        double maleDistance = calculateUserDistance(couple.getMale(), maleCenter);
        double femaleDistance = calculateUserDistance(couple.getFemale(), femaleCenter);
        double rawDistance = (maleDistance + femaleDistance) / 2.0;

        return scaleMinMaxWithRange(
            rawDistance,
            getCoupleClusteringParam().getUserClusterDistDataMin(),
            getCoupleClusteringParam().getUserClusterDistDataMax(),
            getCoupleClusteringParam().getUserClusterDistMin(),
            getCoupleClusteringParam().getUserClusterDistScale()
        );
    }

    private double calculateUserDistance(User user, UserClusterCenter center) {
        UserCluster userCluster = getUserCluster(user);

        double ageDiff = (userCluster.getAgeCoordinate() - center.getAgeCoordinate())
            * getUserClusteringParam().getAgeWeight();
        double mbtiIeDiff = (userCluster.getMbtiIeCoordinate() - center.getMbtiIeCoordinate())
            * getUserClusteringParam().getMbtiIeWeight();
        double mbtiSnDiff = (userCluster.getMbtiSnCoordinate() - center.getMbtiSnCoordinate())
            * getUserClusteringParam().getMbtiSnWeight();
        double mbtiTfDiff = (userCluster.getMbtiTfCoordinate() - center.getMbtiTfCoordinate())
            * getUserClusteringParam().getMbtiTfWeight();
        double mbtiJpDiff = (userCluster.getMbtiJpCoordinate() - center.getMbtiJpCoordinate())
            * getUserClusteringParam().getMbtiJpWeight();
        double budgetDiff = (userCluster.getBudgetCoordinate() - center.getBudgetCoordinate())
            * getUserClusteringParam().getBudgetWeight();

        double ageDistSq = ageDiff * ageDiff;
        double mbtiDistSq = mbtiIeDiff * mbtiIeDiff + mbtiSnDiff * mbtiSnDiff
            + mbtiTfDiff * mbtiTfDiff + mbtiJpDiff * mbtiJpDiff;
        double budgetDistSq = budgetDiff * budgetDiff;

        return Math.sqrt(ageDistSq + mbtiDistSq + budgetDistSq);
    }

    // ========== 6) 유사 커플 플랜 분석 및 추천 가중치 계산 ==========

    private List<Couple> findSimilarCouples(CoupleCluster coupleCluster) {
        // 같은 clusterId + 본인 제외, 최대 50개 랜덤
        PageRequest pageable = PageRequest.of(0, 50);
        return coupleRepository.findRandomSimilarCouples(
            coupleCluster.getClusterId(),
            coupleCluster.getCouple().getId(),
            pageable
        ).getContent();
    }

    /**
     * virginRoad(이미 생성된 경우)가 있으면 IN_PROGRESS나 FINISHED인 항목은 이미 추천 상태로 설정.
     * 그렇지 않으면 기본적으로 [추천=0, 점수합=0] 초기화
     */
    private Map<ServiceType, double[]> initializePlanWeights(VirginRoad virginRoad) {
        Map<ServiceType, double[]> planWeights = new HashMap<>();
        for (ServiceType st : ServiceType.values()) {
            double[] arr = new double[2]; // [0]=추천여부, [1]=점수합
            if (virginRoad != null) {
                Plan plan = virginRoad.findPlanByServiceType(st);
                if (plan != null) {
                    // 이미 BEFORE_START가 아닌 상태면 추천=1로 두어, 해제되지 않게 함
                    arr[0] = (plan.getPlanStatus() == PlanStatus.BEFORE_START) ? 0 : 1;
                }
            }
            planWeights.put(st, arr);
        }
        return planWeights;
    }

    /**
     * 유사 커플들의 플랜을 분석하며, RecommendationConfig를 이용해 점수 계산
     */
    private void analyzeSimilarCouplesPlans(List<Couple> similarCouples,
                                            Couple currentCouple,
                                            Map<ServiceType, double[]> planWeights,
                                            Map<Integer, List<RoadPlanCapsuleDTO>> roadPlanMapByCoupleId) {

        if (similarCouples.isEmpty()) return;

        // 현재 커플의 벡터
        CoupleCluster currCluster = getCoupleCluster(currentCouple);
        double[] currVector = calculateCoupleVector(currCluster);

        // 통계 집계용
        Map<ServiceType, Integer> totalPlans = new HashMap<>();
        Map<ServiceType, Double> totalSimilarities = new HashMap<>();
        for (ServiceType st : planWeights.keySet()) {
            totalPlans.put(st, 0);
            totalSimilarities.put(st, 0.0);
        }

        // 유사 커플 각자 비교
        for (Couple sc : similarCouples) {
            if (sc.equals(currentCouple)) continue;

            try {
                // (1) 유사도 계산
                CoupleCluster scCluster = getCoupleCluster(sc);
                double[] scVector = calculateCoupleVector(scCluster);
                double similarity = recommendationConfig.computeFinalSimilarity(currVector, scVector);

                // (2) DTO 가져오기
                List<RoadPlanCapsuleDTO> scDTOs = roadPlanMapByCoupleId.getOrDefault(sc.getId(), new ArrayList<>());
                if (scDTOs.isEmpty()) continue;

                // (3) ServiceType별로 그룹화
                Map<ServiceType, List<RoadPlanCapsuleDTO>> plansByType = scDTOs.stream()
                    .collect(Collectors.groupingBy(RoadPlanCapsuleDTO::getServiceType));

                // (4) 그 커플의 버진로드 상태
                PlanStatus scVRStatus = scDTOs.get(0).getVirginRoadStatus();

                // (5) type별 점수 계산
                for (Map.Entry<ServiceType, List<RoadPlanCapsuleDTO>> entry : plansByType.entrySet()) {
                    ServiceType st = entry.getKey();
                    // 이미 추천=1인 경우는 더 이상 업데이트 안 함
                    if (!planWeights.containsKey(st) || planWeights.get(st)[0] == 1) {
                        continue;
                    }

                    totalPlans.put(st, totalPlans.get(st) + 1);
                    totalSimilarities.put(st, totalSimilarities.get(st) + similarity);

                    double planScore = recommendationConfig.computePlanScoreForDTOs(
                        entry.getValue(),
                        scVRStatus
                    );
                    // 유사도 가중치 곱
                    planWeights.get(st)[1] += (planScore * similarity);
                }
            } catch (ExceptionHandler e) {
                log.warn("커플 {} 분석 중 오류 발생: {}", sc.getId(), e.getMessage());
            }
        }

        // (6) 최종 추천 여부 결정
        recommendationConfig.determineRecommendations(planWeights, totalPlans, totalSimilarities);
    }

    /**
     * CoupleCluster를 벡터(double[])로 변환
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

    // ========== 7) 엔티티/리포지토리 조회 편의 메서드 ==========

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

    // ========== 스케일링 유틸 메서드 ==========

    private double scaleStandard(double value, double mean, double scale) {
        return (value - mean) / scale;
    }

    private double scaleMinMaxWithRange(double value,
                                        double dataMin,
                                        double dataMax,
                                        double targetMin,
                                        double targetScale) {
        if (dataMax == dataMin) {
            return targetMin;
        }
        double scaled01 = (value - dataMin) / (dataMax - dataMin);
        return targetMin + (scaled01 * targetScale);
    }
}