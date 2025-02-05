package com.ssafy.exhi.domain.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.recommendation.converter.RecommendationConverter;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.recommendation.model.entity.*;
import com.ssafy.exhi.domain.recommendation.repository.ClusterCentersRepository;
import com.ssafy.exhi.domain.recommendation.repository.ClusterRepresentativeRepository;
import com.ssafy.exhi.domain.recommendation.repository.ClusteringParameterRepository;
import com.ssafy.exhi.domain.recommendation.repository.UserClusterRepository;
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
import org.springframework.core.env.Environment;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;
import java.util.stream.IntStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;

/**
 * 추천 서비스 구현 클래스
 * 사용자 클러스터링 및 전시회 추천 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    // === 의존성 주입 ===
    private final UserClusterRepository userClusterRepository;
    private final ClusteringParameterRepository clusteringParameterRepository;
    private final ClusterCentersRepository clusterCentersRepository;
    private final ClusterRepresentativeRepository representativeRepository;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final VirginRoadRepository virginRoadRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Environment env;
    
    // === 환경 설정 변수 ===
    private String jsonOutputDir;
    private String pythonInterpreter;
    private String pythonScript;
    private int pythonScriptTimeout;

    /**
     * 초기 설정을 로드하는 메서드
     */
    @PostConstruct
    public void init() {
        jsonOutputDir = env.getProperty("json.output.dir");
        pythonInterpreter = env.getProperty("python.interpreter");
        
        // Python 스크립트 경로 설정
        String pythonScriptDir = env.getProperty("python.script.dir");
        String pythonScriptName = env.getProperty("python.script.name");
        pythonScript = new File(pythonScriptDir, pythonScriptName).getAbsolutePath();
        
        pythonScriptTimeout = Integer.parseInt(
            env.getProperty("python.script.timeout", "30")
        );
        
        log.info("=== Python Configuration ===");
        log.info("Python Interpreter: {}", pythonInterpreter);
        log.info("Python Script: {}", pythonScript);
        log.info("JSON Output Directory: {}", jsonOutputDir);
        log.info("Script Timeout: {} minutes", pythonScriptTimeout);
    }

    // === 클러스터링 데이터 처리 메서드 (임시 구현) ===
    
    /**
     * 클러스터링을 위한 사용자 데이터 JSON 파일을 생성합니다.
     */
    @Override
    public void generateUserDataJSON() {
        try {
            List<Map<String, Object>> userData = getUserDataFromDB();
            File outputFile = new File(jsonOutputDir, "input_data.json");
            objectMapper.writeValue(outputFile, userData);
            log.info("User data JSON generated: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error generating JSON file: ", e);
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_FILE_IO_ERROR);
        }
    }

    /**
     * Python 클러스터링 스크립트를 실행합니다.
     */
    @Override
    @Transactional
    public void executeClusteringProcess() {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonInterpreter,
                pythonScript
            );
            
            // 작업 디렉토리 설정
            File workingDir = new File(pythonScript).getParentFile();
            processBuilder.directory(workingDir);
            
            // 환경 변수 설정
            Map<String, String> env = processBuilder.environment();
            env.put("RESOURCE_DIR", jsonOutputDir);
            env.put("PYTHONPATH", workingDir.getAbsolutePath());  // PYTHONPATH 설정
            env.put("PYTHONIOENCODING", "utf-8");
            
            // 표준 출력과 에러를 상위 프로세스로 리다이렉트
            processBuilder.redirectErrorStream(true);
            
            // 프로세스 시작
            process = processBuilder.start();
            
            // 실시간으로 출력 읽기
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Python output: {}", line);
                }
            }
            
            if (!process.waitFor(pythonScriptTimeout, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_SCRIPT_TIMEOUT);
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_SCRIPT_ERROR);
            }
            
        } catch (Exception e) {
            log.error("Python script execution error: ", e);
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_SCRIPT_ERROR);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * 클러스터링 결과를 DB에 저장합니다.
     */
    @Override
    @Transactional
    public void processClusteringResults() {
        try {
            // 1. ClusteringParameter 생성 및 저장
            File scalersFile = new File(jsonOutputDir, "scalers.json");
            File weightsFile = new File(jsonOutputDir, "optimized_weights.json");
            
            Map<String, Object> scalers = objectMapper.readValue(scalersFile, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> weights = objectMapper.readValue(weightsFile, new TypeReference<Map<String, Object>>() {});
            
            @SuppressWarnings("unchecked")
            Map<String, Object> age = (Map<String, Object>) scalers.get("age");
            @SuppressWarnings("unchecked")
            Map<String, Object> budget = (Map<String, Object>) scalers.get("budget");
            @SuppressWarnings("unchecked")
            List<Double> mbtiWeights = (List<Double>) weights.get("mbti_weights");
            
            ClusteringParameter parameter = ClusteringParameter.builder()
                .ageMean(((Number) age.get("mean")).doubleValue())
                .ageScale(((Number) age.get("scale")).doubleValue())
                .budgetMin(((Number) budget.get("min")).doubleValue())
                .budgetMax(((Number) budget.get("max")).doubleValue())
                .ageWeight(((Number) weights.get("age_weight")).doubleValue())
                .budgetWeight(((Number) weights.get("budget_weight")).doubleValue())
                .mbtiIeWeight(mbtiWeights.get(0))
                .mbtiSnWeight(mbtiWeights.get(1))
                .mbtiTfWeight(mbtiWeights.get(2))
                .mbtiJpWeight(mbtiWeights.get(3))
                .build();
            
            clusteringParameterRepository.save(parameter);
            
            // 2. ClusterCenters와 ClusterCenter 생성 및 저장
            File centersFile = new File(jsonOutputDir, "cluster_centers.json");
            List<Map<String, Object>> centersList = objectMapper.readValue(centersFile, new TypeReference<List<Map<String, Object>>>() {});
            
            // 새로운 ClusterCenters 생성
            ClusterCenters clusterCenters = new ClusterCenters();
            
            for (Map<String, Object> centerData : centersList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> coordinates = (Map<String, Object>) centerData.get("coordinates");
                @SuppressWarnings("unchecked")
                List<Double> mbtiCoords = (List<Double>) coordinates.get("mbti");
                
                ClusterCenter center = ClusterCenter.builder()
                    .clusterId(((Number) centerData.get("center_id")).intValue())
                    .ageCoordinate(((Number) coordinates.get("age")).doubleValue())
                    .budgetCoordinate(((Number) coordinates.get("budget")).doubleValue())
                    .mbtiIeCoordinate(mbtiCoords.get(0))
                    .mbtiSnCoordinate(mbtiCoords.get(1))
                    .mbtiTfCoordinate(mbtiCoords.get(2))
                    .mbtiJpCoordinate(mbtiCoords.get(3))
                    .build();
                
                clusterCenters.addClusterCenter(center);
            }
            
            clusterCentersRepository.save(clusterCenters);
            
            // 3. UserCluster 업데이트
            File resultsFile = new File(jsonOutputDir, "clustering_results.json");
            Map<String, Object> clusteringResults = objectMapper.readValue(resultsFile, new TypeReference<Map<String, Object>>() {});
            
            // 기존 UserCluster 데이터 삭제
            userClusterRepository.deleteAll();
            
            // 사용자 클러스터 정보 저장
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> userClusters = (List<Map<String, Object>>) clusteringResults.get("user_clusters");
            for (Map<String, Object> userCluster : userClusters) {
                Integer userId = ((Number) userCluster.get("user_id")).intValue();
                Integer clusterId = ((Number) userCluster.get("cluster_id")).intValue();
                
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
                
                // 기존 UserCluster가 있는지 확인
                Optional<UserCluster> existingCluster = userClusterRepository.findByUser(user);
                if (existingCluster.isPresent()) {
                    // 기존 클러스터 정보 업데이트
                    UserCluster cluster = existingCluster.get();
                    cluster.setClusterId(clusterId);
                    userClusterRepository.save(cluster);
                } else {
                    // 새로운 클러스터 정보 생성
                    UserCluster newCluster = UserCluster.builder()
                        .user(user)
                        .clusterId(clusterId)
                        .build();
                    userClusterRepository.save(newCluster);
                }
            }
            
            // 4. ClusterRepresentative 업데이트
            // 기존 대표자 데이터 삭제
            representativeRepository.deleteAll();
            
            // 새로운 대표자 데이터 저장
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> representatives = (List<Map<String, Object>>) clusteringResults.get("cluster_representatives");
            for (Map<String, Object> representative : representatives) {
                Integer clusterId = ((Number) representative.get("cluster_id")).intValue();
                Integer representativeId = ((Number) representative.get("representative_id")).intValue();
                
                User representativeUser = userRepository.findById(representativeId)
                    .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
                
                // 기존 대표자 찾기
                Optional<ClusterRepresentative> existingRep = representativeRepository.findByClusterId(clusterId);
                if (existingRep.isPresent()) {
                    // 기존 대표자 정보 업데이트
                    ClusterRepresentative rep = existingRep.get();
                    rep.setRepresentativeUser(representativeUser);
                    representativeRepository.save(rep);
                } else {
                    // 새로운 대표자 정보 생성
                    ClusterRepresentative newClusterRep = ClusterRepresentative.builder()
                        .clusterId(clusterId)
                        .representativeUser(representativeUser)
                        .build();
                    representativeRepository.save(newClusterRep);
                }
            }
            
            log.info("Successfully processed clustering results and saved to DB");
            
        } catch (IOException e) {
            log.error("Error processing clustering results: ", e);
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_FILE_IO_ERROR);
        } catch (Exception e) {
            log.error("Error saving clustering results to DB: ", e);
            throw new ExceptionHandler(ErrorStatus.DATABASE_INTERNAL_SERVER_ERROR);
        }
    }

    // === 사용자 클러스터 관리 ===

    /**
     * @category ClusteringService
     * @method addUserToCluster
     * @description 사용자를 클러스터에 할당합니다.
     * @param userId 사용자 ID
     * @throws ExceptionHandler USER_NOT_FOUND, RECOMMENDATION_CLUSTER_ERROR
     */
    @Override
    @Transactional
    public void addUserToCluster(Integer userId) {
        // 1. 사용자 조회
        User user = getUserById(userId);
        UserDetail detail = validateUserDetail(user);

        try {
            // 2. 최신 클러스터링 파라미터와 중심점들 조회
            ClusteringParameter params = getLatestClusteringParams();
            ClusterCenters centers = getLatestClusterCenters();

            // 3. 사용자 데이터 정규화
            double normalizedAge = (detail.getAge() - params.getAgeMean()) / params.getAgeScale();
            double normalizedBudget = (detail.getBudget() - params.getBudgetMin()) 
                                    / (params.getBudgetMax() - params.getBudgetMin());

            // 4. MBTI 이진 변환
            double[] mbtiVector = new double[4];
            String mbti = detail.getMbti();
            mbtiVector[0] = mbti.charAt(0) == 'E' ? 1 : 0;  // I/E
            mbtiVector[1] = mbti.charAt(1) == 'N' ? 1 : 0;  // S/N
            mbtiVector[2] = mbti.charAt(2) == 'F' ? 1 : 0;  // T/F
            mbtiVector[3] = mbti.charAt(3) == 'P' ? 1 : 0;  // J/P

            // 5. 사용자의 가중치가 적용된 특성 벡터 생성
            List<Double> userFeatures = Arrays.asList(
                normalizedAge * params.getAgeWeight(),
                normalizedBudget * params.getBudgetWeight(),
                mbtiVector[0] * params.getMbtiIeWeight(),
                mbtiVector[1] * params.getMbtiSnWeight(),
                mbtiVector[2] * params.getMbtiTfWeight(),
                mbtiVector[3] * params.getMbtiJpWeight()
            );

            // 6. 가장 가까운 클러스터 찾기
            int closestCluster = centers.getClusterCenterList().stream()
                .map(center -> new AbstractMap.SimpleEntry<>(
                    center.getClusterId(),
                    calculateDistance(userFeatures, Arrays.asList(
                        center.getAgeCoordinate(),
                        center.getBudgetCoordinate(),
                        center.getMbtiIeCoordinate(),
                        center.getMbtiSnCoordinate(),
                        center.getMbtiTfCoordinate(),
                        center.getMbtiJpCoordinate()
                    ))
                ))
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_CLUSTER_ERROR));

            // 7. 기존 UserCluster 확인 및 업데이트/생성
            Optional<UserCluster> existingCluster = userClusterRepository.findByUser(user);
            if (existingCluster.isPresent()) {
                // 기존 클러스터 정보 업데이트
                UserCluster cluster = existingCluster.get();
                cluster.setClusterId(closestCluster);
                userClusterRepository.save(cluster);
            } else {
                // 새로운 클러스터 정보 생성
                UserCluster newCluster = UserCluster.builder()
                    .user(user)
                    .clusterId(closestCluster)
                    .build();
                userClusterRepository.save(newCluster);
            }

            log.info("User {} assigned to cluster {}", userId, closestCluster);

        } catch (Exception e) {
            log.error("Error assigning user to cluster: ", e);
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_CLUSTER_ERROR);
        }
    }

    // === 추천 서비스 ===

    /**
     * @category RecommendationService
     * @method getRecommendedPlans
     * @description 사용자에게 맞춤형 플랜을 추천합니다.
     * @param userId 사용자 ID
     * @return RecommendedPlansDTO 추천된 플랜 정보
     * @throws ExceptionHandler USER_NOT_FOUND, COUPLE_NOT_FOUND, VIRGIN_ROAD_NOT_FOUND
     */
    @Override
    public RecommendationResponse.RecommendedPlansDTO getRecommendedPlans(Integer userId) {
        User user = getUserById(userId);
        UserCluster userCluster = getUserCluster(user);
        Couple userCouple = getCoupleByUserId(userId);
        VirginRoad userVirginRoad = getVirginRoadByCouple(userCouple);

        // 초기 가중치 맵 생성
        Map<ServiceType, int[]> planWeights = initializePlanWeights(userVirginRoad);

        // 같은 클러스터의 사용자들의 선택 분석
        int sameGroups = analyzeSameClusterChoices(userCluster, planWeights);

        // 추천 임계값 기반으로 가중치 업데이트
        updateWeightsBasedOnThreshold(planWeights, sameGroups);

        // 검증 로직을 서비스 계층에서 수행
        if (userVirginRoad.getPlans() == null) {
            throw new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND);
        }
        if (planWeights == null || planWeights.isEmpty()) {
            throw new ExceptionHandler(ErrorStatus.NOT_ENOUGH_REQUEST_BODY_ERROR);
        }

        // 검증이 완료된 데이터를 Converter에 전달
        return RecommendationConverter.toRecommendedPlansDTO(userVirginRoad, planWeights);
    }

    /**
     * @category RecommendationHelper
     * @method initializePlanWeights
     * @description 버진로드의 플랜들에 대한 초기 가중치 맵을 생성합니다.
     * @param virginRoad 버진로드 엔티티
     * @return Map<ServiceType, int[]> 서비스 타입별 가중치 맵
     */
    private Map<ServiceType, int[]> initializePlanWeights(VirginRoad virginRoad) {
        Map<ServiceType, int[]> planWeights = new HashMap<>();
        for (Plan plan : virginRoad.getPlans()) {
            planWeights.put(plan.getServiceType(), 
                new int[] {plan.getPlanStatus().equals(PlanStatus.BEFORE_START) ? 0 : 1, 0});
        }
        return planWeights;
    }

    /**
     * @category RecommendationHelper
     * @method analyzeSameClusterChoices
     * @description 같은 클러스터의 사용자들의 선택을 분석하고 가중치를 업데이트합니다.
     * @param userCluster 사용자 클러스터
     * @param planWeights 플랜 가중치 맵
     * @return int 같은 클러스터에 속한 사용자 수
     */
    private int analyzeSameClusterChoices(UserCluster userCluster, Map<ServiceType, int[]> planWeights) {
        int sameGroups = 0;
        List<VirginRoad> virginRoads = virginRoadRepository.findAll();
        
        for (VirginRoad virginRoad : virginRoads) {
            Couple couple = virginRoad.getCouple();
            User femaleUser = couple.getFemale();
            UserCluster currentCluster = getUserCluster(femaleUser);
            
            if (currentCluster.getClusterId().equals(userCluster.getClusterId())) {
                sameGroups++;
                updateWeightsFromPlans(virginRoad.getPlans(), planWeights);
            }
        }
        return sameGroups;
    }

    /**
     * 주어진 플랜들의 선택 현황을 기반으로 가중치를 업데이트합니다.
     */
    private void updateWeightsFromPlans(List<Plan> plans, Map<ServiceType, int[]> planWeights) {
        for (Plan plan : plans) {
            if (planWeights.containsKey(plan.getServiceType()) && 
                plan.getPlanStatus().equals(PlanStatus.FINISHED)) {
                planWeights.get(plan.getServiceType())[1]++;
            }
        }
    }

    /**
     * 임계값을 기반으로 최종 가중치를 업데이트합니다.
     */
    private void updateWeightsBasedOnThreshold(Map<ServiceType, int[]> planWeights, int sameGroups) {
        double threshold = sameGroups * 0.4;
        for (Map.Entry<ServiceType, int[]> entry : planWeights.entrySet()) {
            int[] weights = entry.getValue();
            if (weights[0] == 0 && weights[1] >= threshold) {
                weights[0] = 1;
            }
        }
    }

    // === 클러스터링 유틸리티 메서드 ===
    /**
     * @category ClusteringUtility
     * @method calculateDistance
     * @description 두 벡터 간의 유클리드 거리를 계산합니다.
     * @param vector1 첫 번째 벡터
     * @param vector2 두 번째 벡터
     * @return double 두 벡터 간의 거리
     */
    private double calculateDistance(List<Double> vector1, List<Double> vector2) {
        return Math.sqrt(
            IntStream.range(0, vector1.size())
                .mapToDouble(i -> Math.pow(vector1.get(i) - vector2.get(i), 2))
                .sum()
        );
    }

    // === 헬퍼 메서드 ===

    /**
     * DB에서 사용자 데이터 조회
     */
    private List<Map<String, Object>> getUserDataFromDB() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userData = new ArrayList<>();

        for (User user : users) {
            UserDetail detail = user.getUserDetail();
            if (detail == null || detail.getAge() == null || detail.getMbti() == null) {
                continue;
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("user_id", user.getId());
            userMap.put("age", detail.getAge());
            userMap.put("mbti", detail.getMbti());
            userMap.put("budget", detail.getBudget());
            // TODO: 실제 루틴 완료 상태를 확인하는 로직으로 수정 필요
            userMap.put("routine_completed", true);  // 임시로 모든 사용자의 routine_completed를 true로 설정
            userData.add(userMap);
        }

        return userData;
    }

    /**
     * @category EntityHelper
     * @method getUserById
     * @description ID로 사용자를 조회합니다.
     * @param userId 사용자 ID
     * @return User 사용자 엔티티
     * @throws ExceptionHandler USER_NOT_FOUND
     */
    private User getUserById(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }

    /**
     * 사용자 상세 정보 유효성 검사
     */
    private UserDetail validateUserDetail(User user) {
        UserDetail detail = user.getUserDetail();
        if (detail == null || detail.getAge() == null || detail.getMbti() == null) {
            throw new ExceptionHandler(ErrorStatus.RECOMMENDATION_USER_DETAIL_NOT_FOUND);
        }
        return detail;
    }

    /**
     * 최신 클러스터링 파라미터 조회
     */
    private ClusteringParameter getLatestClusteringParams() {
        return clusteringParameterRepository.findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_PARAMS_NOT_FOUND));
    }

    /**
     * 최신 클러스터 중심점 조회
     */
    private ClusterCenters getLatestClusterCenters() {
        return clusterCentersRepository.findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_CENTERS_NOT_FOUND));
    }

    /**
     * @category EntityHelper
     * @method getUserCluster
     * @description 사용자의 클러스터 정보를 조회합니다.
     * @param user 사용자 엔티티
     * @return UserCluster 사용자 클러스터 정보
     * @throws ExceptionHandler RECOMMENDATION_CLUSTER_NOT_FOUND
     */
    private UserCluster getUserCluster(User user) {
        return userClusterRepository.findByUser(user)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.RECOMMENDATION_CLUSTER_NOT_FOUND));
    }

    /**
     * 사용자 ID로 커플 정보 조회
     */
    private Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
    }

    /**
     * 커플 정보로 버진로드 조회
     */
    private VirginRoad getVirginRoadByCouple(Couple couple) {
        return virginRoadRepository.getVirginRoadByCouple(couple)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_NOT_FOUND));
    }
}