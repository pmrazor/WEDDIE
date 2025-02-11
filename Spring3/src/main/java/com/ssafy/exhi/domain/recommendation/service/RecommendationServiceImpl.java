package com.ssafy.exhi.domain.recommendation.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.recommendation.converter.RecommendationConverter;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.recommendation.model.entity.*;
import com.ssafy.exhi.domain.recommendation.repository.*;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    // === Repository Dependencies ===
    private final UserRepository userRepository;
    private final UserClusterRepository userClusterRepository;
    private final UserClusterCentersRepository userClusterCentersRepository;
    private final UserClusteringParameterRepository userClusteringParameterRepository;
    private final CoupleRepository coupleRepository;
    private final CoupleClusterRepository coupleClusterRepository;
    private final CoupleClusterCentersRepository coupleClusterCentersRepository;
    private final CoupleClusteringParameterRepository coupleClusteringParameterRepository;
    private final VirginRoadRepository virginRoadRepository;

    // === Cached Parameters ===
    private volatile UserClusteringParameter userClusteringParam;
    private volatile CoupleClusteringParameter coupleClusteringParam;

    // 동기화를 위한 락 객체
    private final Object lock = new Object();

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

    // === Public API Methods ===

    /**
     * 사용자와 관련된 모든 클러스터를 처리하는 메서드
     */
    @Override
    @Transactional
    public void addUserToCluster(Integer userId) {
        // 사용자와 해당 사용자의 커플 정보 조회
        User user = getUserById(userId);
        Couple couple = getCoupleByUserId(userId);

        // 현재 사용자의 클러스터 생성 또는 조회 후 할당
        UserCluster userCluster = findOrCreateUserCluster(user);
        assignUserToCluster(userCluster, user);
        userClusterRepository.save(userCluster);

        // 파트너 사용자의 클러스터 처리
        User partnerUser = (user.equals(couple.getMale())) ? couple.getFemale() : couple.getMale();
        UserCluster partnerCluster = findOrCreateUserCluster(partnerUser);
        assignUserToCluster(partnerCluster, partnerUser);
        userClusterRepository.save(partnerCluster);

        // 커플 클러스터 처리
        CoupleCluster coupleCluster = findOrCreateCoupleCluster(couple);
        calculateCoupleCoordinates(coupleCluster, couple);
        assignCoupleToCluster(coupleCluster, couple);
        coupleClusterRepository.save(coupleCluster);
    }

    /**
     * 사용자에게 맞춤형 Plan을 추천하는 메서드
     */
    public RecommendationResponse.RecommendedPlansDTO getRecommendedPlans(Integer userId) {
        // 커플과 버진로드 정보 조회
        Couple couple = getCoupleByUserId(userId);
        VirginRoad virginRoad = getVirginRoadByCouple(couple);

        // 각 서비스 타입별 추천 가중치 초기화
        Map<ServiceType, double[]> planWeights = initializePlanWeights(virginRoad);

        // 유사한 커플들의 Plan 데이터를 분석하여 추천 가중치 계산
        analyzeSimilarCouplesPlans(findSimilarCouples(getCoupleCluster(couple)), couple, planWeights);

        // 최종 추천 결과를 DTO로 변환하여 반환
        return RecommendationConverter.toRecommendedPlansDTO(virginRoad, planWeights);
    }

    // === Cluster Assignment Methods ===

    /**
     * 사용자의 클러스터를 조회하거나 새로 생성하는 메서드
     */
    private UserCluster findOrCreateUserCluster(User user) {
        // 기존 클러스터가 있으면 반환, 없으면 새로 생성
        return userClusterRepository.findByUser(user)
                .orElse(UserCluster.builder()
                        .user(user)
                        .build());
    }

    /**
     * 커플의 클러스터를 조회하거나 새로 생성하는 메서드
     */
    private CoupleCluster findOrCreateCoupleCluster(Couple couple) {
        // 기존 클러스터가 있으면 반환, 없으면 새로 생성
        return coupleClusterRepository.findByCouple(couple)
                .orElse(CoupleCluster.builder()
                        .couple(couple)
                        .build());
    }

    /**
     * 사용자를 가장 가까운 클러스터에 할당하는 메서드
     */
    private void assignUserToCluster(UserCluster userCluster, User user) {
        // 최신 클러스터 중심점 정보 조회
        UserClusterCenters centers = userClusterCentersRepository.findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS));

        // 사용자의 특성을 좌표값으로 변환
        calculateUserCoordinates(userCluster, user);

        // 가장 가까운 클러스터 찾기
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

    /**
     * 커플을 가장 가까운 클러스터에 할당하는 메서드
     */
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

    /**
     * 유클리드 거리를 계산하는 메서드
     */
    private double calculateEuclideanDistance(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double sumSquaredDiff = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff);
    }

    /**
     * 사용자의 특성을 정규화된 좌표값으로 변환하는 메서드
     */
    private void calculateUserCoordinates(UserCluster userCluster, User user) {
        // 사용자 상세 정보 확인
        UserDetail detail = user.getUserDetail();
        if (detail == null) {
            throw new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND);
        }

        // 클러스터링 파라미터 조회
        UserClusteringParameter param = getUserClusteringParam();

        // 나이 좌표 계산
        userCluster.setAgeCoordinate(
            scaleStandard(detail.getAge(), param.getAgeMean(), param.getAgeScale())
        );

        // 예산 좌표 계산
        userCluster.setBudgetCoordinate(
            scaleMinMaxWithRange(
                getCoupleByUserId(user.getId()).getBudget(),
                param.getBudgetDataMin(),
                param.getBudgetDataMax(),
                param.getBudgetMin(),
                param.getBudgetScale()
            )
        );

        // MBTI 좌표 계산 (각 지표별로 이진값 할당)
        String mbti = detail.getMbti();
        if (mbti != null && mbti.length() == 4) {
            userCluster.setMbtiIeCoordinate(mbti.charAt(0) == 'E' ? 1.0 : 0.0);
            userCluster.setMbtiSnCoordinate(mbti.charAt(1) == 'S' ? 1.0 : 0.0);
            userCluster.setMbtiTfCoordinate(mbti.charAt(2) == 'T' ? 1.0 : 0.0);
            userCluster.setMbtiJpCoordinate(mbti.charAt(3) == 'J' ? 1.0 : 0.0);
        } else {
            // MBTI 정보가 없는 경우 기본값 설정
            userCluster.setMbtiIeCoordinate(-1.0);
            userCluster.setMbtiSnCoordinate(-1.0);
            userCluster.setMbtiTfCoordinate(-1.0);
            userCluster.setMbtiJpCoordinate(-1.0);
        }
    }

    /**
     * 커플의 좌표값을 계산하여 CoupleCluster에 저장하는 메서드
     */
    private void calculateCoupleCoordinates(CoupleCluster coupleCluster, Couple couple) {
        // 클러스터링 매개변수 세팅
        CoupleClusteringParameter param = getCoupleClusteringParam();

        // 남편/아내 나이를 StandardScaler로 변환
        coupleCluster.setMaleAgeCoordinate(
            scaleStandard(couple.getMale().getUserDetail().getAge(), param.getMaleAgeMean(), param.getMaleAgeScale())
        );

        coupleCluster.setFemaleAgeCoordinate(
            scaleStandard(couple.getFemale().getUserDetail().getAge(), param.getFemaleAgeMean(), param.getFemaleAgeScale())
        );

        // 나이 차이를 계산하여 좌표로 할당
        int ageDiff = couple.getMale().getUserDetail().getAge() - 
                     couple.getFemale().getUserDetail().getAge();
        coupleCluster.setAgeDiffCoordinate(
            scaleMinMaxWithRange(
                ageDiff,
                param.getAgeDiffDataMin(),
                param.getAgeDiffDataMax(),
                param.getAgeDiffMin(),
                param.getAgeDiffScale()
            )
        );

        // 예산을 좌표로 할당
        coupleCluster.setBudgetCoordinate(
            scaleMinMaxWithRange(
                couple.getBudget(),
                param.getBudgetDataMin(),
                param.getBudgetDataMax(),
                param.getBudgetMin(),
                param.getBudgetScale()
            )
        );

        // 결혼 날짜 관련 좌표들 계산
        LocalDate marriageDate = couple.getMarriageDate();
        int month = marriageDate.getMonthValue();
        int day = marriageDate.getDayOfMonth();

        // 월 sin/cos 좌표 계산
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

        // 일 sin/cos 좌표 계산
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

        // 유저 클러스터 거리 계산 및 스케일링
        double userClusterDistance = calculateUserClusterDistance(couple);
        coupleCluster.setUserClusterDistanceCoordinate(userClusterDistance);
    }

    // === Distance Calculation Methods ===

    /**
     * 사용자와 클러스터 중심점 간의 거리를 계산하는 메서드
     */
    private double calculateUserDistance(User user, UserClusterCenter center, Couple couple) {
        // 사용자 상세 정보 확인
        UserDetail detail = user.getUserDetail();
        if (detail == null) {
            throw new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND);
        }

        UserCluster userCluster = getUserCluster(user);
        
        // 나이 거리 계산
        double ageDiff = (userCluster.getAgeCoordinate() - center.getAgeCoordinate()) 
                         * getUserClusteringParam().getAgeWeight();
        double ageDistSq = ageDiff * ageDiff;

        // MBTI 거리 계산
        double mbtiIeDiff = (userCluster.getMbtiIeCoordinate() - center.getMbtiIeCoordinate()) 
                            * getUserClusteringParam().getMbtiIeWeight();
        double mbtiSnDiff = (userCluster.getMbtiSnCoordinate() - center.getMbtiSnCoordinate()) 
                            * getUserClusteringParam().getMbtiSnWeight();
        double mbtiTfDiff = (userCluster.getMbtiTfCoordinate() - center.getMbtiTfCoordinate()) 
                            * getUserClusteringParam().getMbtiTfWeight();
        double mbtiJpDiff = (userCluster.getMbtiJpCoordinate() - center.getMbtiJpCoordinate()) 
                            * getUserClusteringParam().getMbtiJpWeight();
        
        double mbtiDistSq = mbtiIeDiff * mbtiIeDiff + mbtiSnDiff * mbtiSnDiff + 
                           mbtiTfDiff * mbtiTfDiff + mbtiJpDiff * mbtiJpDiff;

        // 예산 거리 계산
        double budgetDiff = (userCluster.getBudgetCoordinate() - center.getBudgetCoordinate()) 
                            * getUserClusteringParam().getBudgetWeight();
        double budgetDistSq = budgetDiff * budgetDiff;

        // 최종 거리 계산
        return Math.sqrt(ageDistSq + mbtiDistSq + budgetDistSq);
    }

    /**
     * 커플 간의 거리를 계산하는 메서드
     * 각 특성별 가중치를 적용하여 최종 거리값 산출
     */
    private double calculateCoupleDistance(Couple couple, CoupleClusterCenter center) {
        CoupleCluster coupleCluster = getCoupleCluster(couple);
        CoupleClusteringParameter param = getCoupleClusteringParam();

        // 남편 나이 거리 계산
        double maleDiff = (coupleCluster.getMaleAgeCoordinate() - center.getMaleAgeCoordinate()) 
                          * param.getMaleAgeWeight();
        double maleDistSq = maleDiff * maleDiff;

        // 아내 나이 거리 계산
        double femaleDiff = (coupleCluster.getFemaleAgeCoordinate() - center.getFemaleAgeCoordinate()) 
                            * param.getFemaleAgeWeight();
        double femaleDistSq = femaleDiff * femaleDiff;

        // 나이 차이 거리 계산
        double ageDiffDist = (coupleCluster.getAgeDiffCoordinate() - center.getAgeDiffCoordinate()) 
                             * param.getAgeDiffWeight();
        double ageDiffDistSq = ageDiffDist * ageDiffDist;

        // 예산 거리 계산
        double budgetDiff = (coupleCluster.getBudgetCoordinate() - center.getBudgetCoordinate()) 
                            * param.getBudgetWeight();
        double budgetDistSq = budgetDiff * budgetDiff;

        // 결혼 날짜 관련 거리 계산
        double monthSinDiff = (coupleCluster.getMarriageMonthSinCoordinate() - center.getMarriageMonthSinCoordinate()) 
                              * param.getMarriageMonthSinWeight();
        double monthCosDiff = (coupleCluster.getMarriageMonthCosCoordinate() - center.getMarriageMonthCosCoordinate()) 
                              * param.getMarriageMonthCosWeight();
        double daySinDiff = (coupleCluster.getMarriageDaySinCoordinate() - center.getMarriageDaySinCoordinate()) 
                            * param.getMarriageDaySinWeight();
        double dayCosDiff = (coupleCluster.getMarriageDayCosCoordinate() - center.getMarriageDayCosCoordinate()) 
                            * param.getMarriageDayCosWeight();

        double marriageDistSq = monthSinDiff * monthSinDiff + monthCosDiff * monthCosDiff +
                               daySinDiff * daySinDiff + dayCosDiff * dayCosDiff;

        // 유저 클러스터 거리 계산
        double userClusterDist = (coupleCluster.getUserClusterDistanceCoordinate() - center.getUserClusterDistanceCoordinate()) 
                                 * param.getUserClusterDistanceWeight();
        double userClusterDistSq = userClusterDist * userClusterDist;

        // 최종 거리값 계산
        return Math.sqrt(maleDistSq + femaleDistSq + ageDiffDistSq + budgetDistSq + 
                        marriageDistSq + userClusterDistSq);
    }

    /**
     * 커플의 유저 클러스터 간 거리를 계산하는 메서드
     * 남성과 여성 각각의 클러스터 거리를 평균내어 계산
     */
    private double calculateUserClusterDistance(Couple couple) {
        UserCluster maleCluster = getUserCluster(couple.getMale());
        UserCluster femaleCluster = getUserCluster(couple.getFemale());

        // 클러스터 중심점 정보 조회
        UserClusterCenters centers = userClusterCentersRepository
            .findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        // 각 사용자의 클러스터 중심점 찾기
        UserClusterCenter maleCenter = centers.getUserClusterCenterList().stream()
                .filter(c -> Objects.equals(c.getClusterId(), maleCluster.getClusterId()))
                .findFirst()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        UserClusterCenter femaleCenter = centers.getUserClusterCenterList().stream()
                .filter(c -> Objects.equals(c.getClusterId(), femaleCluster.getClusterId()))
                .findFirst()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));

        // 각 사용자의 클러스터 거리 계산
        double maleDistance = calculateUserDistance(couple.getMale(), maleCenter, couple);
        double femaleDistance = calculateUserDistance(couple.getFemale(), femaleCenter, couple);
        
        // 평균 거리 계산 및 스케일링
        double rawDistance = (maleDistance + femaleDistance) / 2.0;
        
        return scaleMinMaxWithRange(
            rawDistance,
            getCoupleClusteringParam().getUserClusterDistDataMin(),
            getCoupleClusteringParam().getUserClusterDistDataMax(),
            getCoupleClusteringParam().getUserClusterDistMin(),
            getCoupleClusteringParam().getUserClusterDistScale()
        );
    }

    // === Recommendation Logic Methods ===

    /**
     * 유사한 커플들을 찾는 메서드
     * 같은 클러스터에 속한 다른 커플들을 반환
     */
    private List<Couple> findSimilarCouples(CoupleCluster coupleCluster) {
        return coupleRepository.findAllByClusterId(coupleCluster.getClusterId()).stream()
                .filter(couple -> !couple.equals(coupleCluster.getCouple()))
                .toList();
    }

    /**
     * 각 서비스 타입별 추천 가중치를 초기화하는 메서드
     * 이미 시작된 Plan은 가중치를 1로, 시작되지 않은 Plan은 0으로 초기화
     */
    private Map<ServiceType, double[]> initializePlanWeights(VirginRoad virginRoad) {
        Map<ServiceType, double[]> planWeights = new HashMap<>();

        for (ServiceType serviceType : ServiceType.values()) {
            Plan plan = virginRoad.findPlanByServiceType(serviceType);
            if (plan != null) {
                double[] weights = new double[2];
                weights[0] = plan.getPlanStatus() == PlanStatus.BEFORE_START ? 0 : 1;
                weights[1] = 0;
                planWeights.put(serviceType, weights);
            }
        }

        return planWeights;
    }

    /**
     * 코사인 유사도를 계산하고 [0,1] 범위로 정규화하는 메서드
     */
    private double calculateCosineSimilarityNormalized(double[] vector1, double[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        // 0벡터 처리
        if (norm1 == 0 || norm2 == 0) {
            return 0.5; // 중간값으로 처리
        }
        
        // 코사인 값 계산 [-1,1]
        double cosineValue = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        
        // [0,1] 범위로 정규화
        return (cosineValue + 1.0) / 2.0;
    }

    /**
     * 유클리드 거리 기반 유사도를 [0,1] 범위로 계산하는 메서드
     */
    private double calculateDistanceSimilarity(CoupleCluster cluster1, CoupleCluster cluster2) {
        double[] vector1 = calculateCoupleVector(cluster1);
        double[] vector2 = calculateCoupleVector(cluster2);
        
        double sumSquaredDiff = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sumSquaredDiff += diff * diff;
        }
        
        double distance = Math.sqrt(sumSquaredDiff);
        return 1.0 / (1.0 + distance); // 거리가 0이면 1, 거리가 커질수록 0에 수렴
    }

    /**
     * 두 유사도를 가중 평균으로 결합하는 메서드
     */
    private double combineSimilarities(double cosineSimilarity, double distanceSimilarity, double alpha) {
        return alpha * cosineSimilarity + (1 - alpha) * distanceSimilarity;
    }

    /**
     * 유사한 커플들의 Plan을 분석하여 추천 가중치를 계산하는 메서드
     */
    private void analyzeSimilarCouplesPlans(List<Couple> similarCouples, Couple currentCouple, 
                                          Map<ServiceType, double[]> planWeights) {
        if (similarCouples.isEmpty()) return;
        
        CoupleCluster currentCoupleCluster = getCoupleCluster(currentCouple);
        double[] currentVector = calculateCoupleVector(currentCoupleCluster);
        
        // 각 서비스 타입별 통계 초기화
        Map<ServiceType, Integer> totalPlans = new HashMap<>();
        for (ServiceType type : planWeights.keySet()) {
            totalPlans.put(type, 0);
        }

        // 유사도 계산 가중치 (코사인:거리 = 7:3)
        final double COSINE_WEIGHT = 0.7;

        for (Couple similarCouple : similarCouples) {
            if (similarCouple.equals(currentCouple)) continue;

            try {
                CoupleCluster similarCoupleCluster = getCoupleCluster(similarCouple);
                double[] similarVector = calculateCoupleVector(similarCoupleCluster);

                // 정규화된 유사도 계산
                double cosineSimilarity = calculateCosineSimilarityNormalized(currentVector, similarVector);
                double distanceSimilarity = calculateDistanceSimilarity(currentCoupleCluster, similarCoupleCluster);
                
                // 가중 평균으로 최종 유사도 계산
                double finalSimilarity = combineSimilarities(cosineSimilarity, distanceSimilarity, COSINE_WEIGHT);

                // 유사 커플의 Plan 분석
                VirginRoad virginRoad = getVirginRoadByCouple(similarCouple);
                for (Plan plan : virginRoad.getPlans()) {
                    ServiceType serviceType = plan.getServiceType();
                    if (!planWeights.containsKey(serviceType) || planWeights.get(serviceType)[0] == 1) {
                        continue;
                    }

                    totalPlans.put(serviceType, totalPlans.get(serviceType) + 1);
                    double planScore = calculatePlanScore(plan, virginRoad.getVirginRoadStatus());
                    planWeights.get(serviceType)[1] += planScore * finalSimilarity;
                }
            } catch (ExceptionHandler e) {
                log.warn("Error analyzing couple {}: {}", similarCouple.getId(), e.getMessage());
                continue;
            }
        }

        determineRecommendations(planWeights, totalPlans);
    }

    /**
     * 커플의 특성을 벡터로 변환하는 메서드
     */
    private double[] calculateCoupleVector(CoupleCluster coupleCluster) {
        // 모든 좌표값을 벡터로 변환
        return new double[] {
            coupleCluster.getMaleAgeCoordinate(),
            coupleCluster.getFemaleAgeCoordinate(),
            coupleCluster.getAgeDiffCoordinate(),
            coupleCluster.getBudgetCoordinate(),
            coupleCluster.getMarriageMonthSinCoordinate(),
            coupleCluster.getMarriageMonthCosCoordinate(),
            coupleCluster.getMarriageDaySinCoordinate(),
            coupleCluster.getMarriageDayCosCoordinate(),
            coupleCluster.getUserClusterDistanceCoordinate()
        };
    }

    /**
     * Plan의 점수를 계산하는 메서드
     * 플랜 상태와 타임캡슐 정보를 기반으로 점수 산출
     */
    private double calculatePlanScore(Plan plan, PlanStatus virginRoadStatus) {
        double score = 0.0;
        
        // 기본 점수 계산 (플랜 상태 기준)
        switch (plan.getPlanStatus()) {
            case FINISHED -> {
                score = 2.0;
                // 타임캡슐이 있는 경우에만 추가 점수
                if (!plan.getTimeCapsules().isEmpty()) {
                    double capsuleScore = calculateTimeCapsuleScore(plan.getTimeCapsules());
                    score += capsuleScore;
                }
                // 버진로드가 완료된 경우 추가 가중치
                if (virginRoadStatus == PlanStatus.FINISHED) {
                    score *= 1.5;
                }
            }
            case IN_PROGRESS -> score = 1.0;
            case BEFORE_START -> score = 0.0;
        }
        
        return score;
    }

    /**
     * 타임캡슐의 점수를 계산하는 메서드
     * 긍정적/부정적 평가를 기반으로 점수 산출
     */
    private double calculateTimeCapsuleScore(List<TimeCapsule> timeCapsules) {
        if (timeCapsules.isEmpty()) return 0.0;
        
        // 긍정적 평가는 2점, 부정적 평가는 -1점으로 계산
        int weight = timeCapsules.stream()
                .mapToInt(tc -> tc.isPlanGrade() ? -1 : 2)
                .sum();
        
        // 타임캡슐 점수를 0.0 ~ 1.0 범위로 정규화
        return Math.min(Math.max(weight / 5.0, 0.0), 1.0);
    }

    /**
     * 최종 추천 여부를 결정하는 메서드
     * 각 서비스 타입별 점수를 분석하여 추천 여부 결정
     */
    private void determineRecommendations(Map<ServiceType, double[]> planWeights, 
                                        Map<ServiceType, Integer> totalPlans) {
        double recommendationThreshold = 12.0;  // 12% 이상일 때 추천
        
        for (Map.Entry<ServiceType, double[]> entry : planWeights.entrySet()) {
            ServiceType serviceType = entry.getKey();
            double[] weights = entry.getValue();
            
            // 이미 시작된 플랜이거나 데이터가 없는 경우 스킵
            if (weights[0] == 1 || totalPlans.get(serviceType) == 0) continue;
            
            // 최대 가능 점수 = 플랜 수 * (기본 최대 점수 + 최대 타임캡슐 점수) * 버진로드 완료 가중치
            double maxPossibleScore = totalPlans.get(serviceType) * (2.0 + 1.0) * 1.5;
            double percentage = (weights[1] / maxPossibleScore) * 100.0;
            
            // 임계값 이상인 경우 추천으로 설정
            if (percentage >= recommendationThreshold) {
                weights[0] = 1;
            }
        }
    }

    // === Helper Methods ===

    private int findNearestUserCluster(User user, UserClusterCenters centers) {
        Couple couple = getCoupleByUserId(user.getId());
        return centers.getUserClusterCenterList().stream()
                .min(Comparator.comparingDouble(center -> calculateUserDistance(user, center, couple)))
                .map(UserClusterCenter::getClusterId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS));
    }

    private int findNearestCoupleCluster(Couple couple, CoupleClusterCenters centers) {
        return centers.getCoupleClusterCenterList().stream()
                .min(Comparator.comparingDouble(center -> calculateCoupleDistance(couple, center)))
                .map(CoupleClusterCenter::getClusterId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_NO_CLUSTER_CENTERS));
    }

    // === Entity Retrieval Methods ===

    private User getUserById(Integer userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }

    private UserCluster getUserCluster(User user) {
        return userClusterRepository.findByUser(user)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_CLUSTER_NOT_FOUND));
    }

    private Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_DATA_NOT_READY));
    }

    private CoupleCluster getCoupleCluster(Couple couple) {
        return coupleClusterRepository.findByCouple(couple)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_CLUSTER_NOT_FOUND));
    }

    private VirginRoad getVirginRoadByCouple(Couple couple) {
        return virginRoadRepository.getVirginRoadByCouple(couple)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_NOT_FOUND));
    }

    // === Scaling Methods ===

    /**
     * 표준 스케일링을 수행하는 메서드
     */
    private double scaleStandard(double value, double mean, double scale) {
        return (value - mean) / scale;
    }

    /**
     * MinMaxScaler와 커스텀 범위를 적용하는 메서드
     */
    private double scaleMinMaxWithRange(double value, double dataMin, double dataMax, 
                                      double targetMin, double targetScale) {
        // 먼저 0-1 스케일로 변환
        double scaled01 = (value - dataMin) / (dataMax - dataMin);
        // 그 다음 목표 범위로 변환
        return targetMin + (scaled01 * targetScale);
    }
}