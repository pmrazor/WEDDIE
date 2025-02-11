package com.ssafy.exhi.domain.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.recommendation.repository.*;
import com.ssafy.exhi.domain.recommendation.model.entity.*;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClusteringDataServiceImpl implements ClusteringDataService {

    // 의존성 주입
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserClusterRepository userClusterRepository;
    private final UserClusterCentersRepository userClusterCentersRepository;
    private final UserClusteringParameterRepository userClusteringParameterRepository;
    private final CoupleRepository coupleRepository;
    private final CoupleClusterRepository coupleClusterRepository;
    private final CoupleClusterCentersRepository coupleClusterCentersRepository;
    private final CoupleClusteringParameterRepository coupleClusteringParameterRepository;

    
    @Value("${python.script.dir}/${python.script.name}")
    private String pythonScriptPath;

    @Value("${json.output.dir}")
    private String jsonOutputDir;

    @Value("${python.interpreter}")
    private String pythonInterpreter;

    @Value("${python.script.timeout:30}")
    private int pythonScriptTimeout;

    @PostConstruct
    private void configureObjectMapper() {
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        
        // JSON 출력 디렉토리 생성
        createDirectoryIfNotExists(jsonOutputDir);
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created directory: {}", directoryPath);
            } else {
                log.error("Failed to create directory: {}", directoryPath);
                throw new ExceptionHandler(ErrorStatus.CLUSTERING_FILE_IO_ERROR);
            }
        }
    }

    /**
     * DB에서 클러스터링에 필요한 데이터를 가져와 JSON 파일로 생성합니다.
     * 생성된 파일은 Python 스크립트의 입력으로 사용됩니다.
     * @throws ExceptionHandler 데이터가 없거나 파일 생성 중 오류 발생 시
     */
    @Override
    public void generateInputDataJSON() {
        try {
            List<Map<String, Object>> coupleData = getUserDataFromDB();
            if (coupleData.isEmpty()) {
                throw new ExceptionHandler(ErrorStatus.CLUSTERING_NO_DATA);
            }
            
            File outputFile = new File(jsonOutputDir, "input_data.json");
            objectMapper.writeValue(outputFile, coupleData);
            log.info("Input data JSON generated: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error generating JSON file: ", e);
            throw new ExceptionHandler(ErrorStatus.CLUSTERING_FILE_IO_ERROR);
        }
    }

    /**
     * Python 클러스터링 스크립트를 실행합니다.
     * @throws ExceptionHandler 스크립트 실행 중 오류 발생 시
     */
    @Override
    public void executeClusteringScript() {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonInterpreter,
                pythonScriptPath
            );
            
            // 작업 디렉토리 설정
            File workingDir = new File(pythonScriptPath).getParentFile();
            processBuilder.directory(workingDir);
            
            // 환경 변수 설정
            Map<String, String> env = processBuilder.environment();
            env.put("RESOURCE_DIR", jsonOutputDir);
            env.put("PYTHONPATH", workingDir.getAbsolutePath());
            env.put("PYTHONIOENCODING", "utf-8");

            log.info("Python interpreter: {}", pythonInterpreter);
            log.info("Python script path: {}", pythonScriptPath);
            log.info("Working directory: {}", workingDir.getAbsolutePath());
            log.info("RESOURCE_DIR: {}", env.get("RESOURCE_DIR"));
            log.info("PYTHONPATH: {}", env.get("PYTHONPATH"));
            log.info("PYTHONIOENCODING: {}", env.get("PYTHONIOENCODING"));

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
            
            // 타임아웃 처리
            if (!process.waitFor(pythonScriptTimeout, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new ExceptionHandler(ErrorStatus.CLUSTERING_SCRIPT_TIMEOUT);
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Python script failed with exit code: {}", exitCode);
                throw new ExceptionHandler(ErrorStatus.CLUSTERING_SCRIPT_ERROR);
            }
            
        } catch (Exception e) {
            log.error("Python script execution error: ", e);
            throw new ExceptionHandler(ErrorStatus.CLUSTERING_SCRIPT_ERROR);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * 클러스터링 결과를 처리하고 DB에 저장합니다.
     * @throws ExceptionHandler 결과 처리 중 오류 발생 시
     */
    @Override
    @Transactional
    public void processClusteringResults() {
        try {
            validateClusteringFiles();
            saveClusterAssignments();
            saveClusteringParameters();
            log.info("Successfully processed clustering results");
        } catch (IOException e) {
            log.error("Error processing clustering results: ", e);
            throw new ExceptionHandler(ErrorStatus.CLUSTERING_FILE_IO_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error during clustering: ", e);
            throw new ExceptionHandler(ErrorStatus.CLUSTERING_CLUSTER_ERROR);
        }
    }

    // 데이터 처리 메서드
    private List<Map<String, Object>> getUserDataFromDB() {
        List<Couple> couples = coupleRepository.findAll();
        List<Map<String, Object>> coupleData = new ArrayList<>();

        for (Couple couple : couples) {
            try {
                User femaleUser = Optional.ofNullable(couple.getFemale())
                    .orElseThrow(() -> new ExceptionHandler(ErrorStatus.CLUSTERING_USER_DETAIL_NOT_FOUND));
                User maleUser = Optional.ofNullable(couple.getMale())
                    .orElseThrow(() -> new ExceptionHandler(ErrorStatus.CLUSTERING_USER_DETAIL_NOT_FOUND));

                UserDetail femaleDetail = validateUserDetail(femaleUser);
                UserDetail maleDetail = validateUserDetail(maleUser);

                Map<String, Object> femaleMap = new HashMap<>();
                femaleMap.put("user_id", femaleUser.getId());
                femaleMap.put("age", femaleDetail.getAge());
                femaleMap.put("mbti", validateMbti(femaleDetail.getMbti()));

                Map<String, Object> maleMap = new HashMap<>();
                maleMap.put("user_id", maleUser.getId());
                maleMap.put("age", maleDetail.getAge());
                maleMap.put("mbti", validateMbti(maleDetail.getMbti()));

                Map<String, Object> coupleMap = new HashMap<>();
                coupleMap.put("couple_id", couple.getId());
                coupleMap.put("female", femaleMap);
                coupleMap.put("male", maleMap);
                coupleMap.put("budget", couple.getBudget());

                LocalDate marriageDate = couple.getMarriageDate();
                String formattedDate = marriageDate.format(DateTimeFormatter.ofPattern("MM-dd"));
                coupleMap.put("marriage_date", formattedDate);

                coupleData.add(coupleMap);
            } catch (Exception e) {
                log.warn("Skipping couple {} due to: {}", couple.getId(), e.getMessage());
            }
        }

        if (coupleData.isEmpty()) {
            throw new ExceptionHandler(ErrorStatus.CLUSTERING_NO_DATA);
        }

        return coupleData;
    }

    /**
     * 클러스터 할당 결과를 DB에 저장합니다.
     * User와 Couple 각각의 클러스터 정보를 업데이트합니다.
     */
    private void saveClusterAssignments() throws IOException {
        // ---------------------------
        // 1) User assignments 저장
        // ---------------------------
        File userAssignmentsFile = new File(jsonOutputDir, "user_assignments.json");
        List<Map<String, Object>> userAssignments = objectMapper.readValue(
            userAssignmentsFile,
            new TypeReference<List<Map<String, Object>>>() {}
        );

        for (Map<String, Object> assignment : userAssignments) {
            Integer userId = ((Number) assignment.get("userid")).intValue();
            Integer clusterId = ((Number) assignment.get("cluster")).intValue();

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));

            // 1) 기존 UserCluster 있으면 재사용, 없으면 새로 생성
            UserCluster userCluster = user.getUserCluster();
            if (userCluster == null) {
                userCluster = UserCluster.builder()
                    .user(user)
                    .build();
            }
            // 2) 클러스터 ID를 매번 새로 덮어쓰기
            userCluster.setClusterId(clusterId);

            // weighted_features 배열에서 좌표값 추출
            List<Double> weightedFeatures = (List<Double>) assignment.get("weighted_features");
            userCluster.setAgeCoordinate(weightedFeatures.get(0));
            userCluster.setBudgetCoordinate(weightedFeatures.get(1));
            userCluster.setMbtiIeCoordinate(weightedFeatures.get(2));
            userCluster.setMbtiSnCoordinate(weightedFeatures.get(3));
            userCluster.setMbtiTfCoordinate(weightedFeatures.get(4));
            userCluster.setMbtiJpCoordinate(weightedFeatures.get(5));

            // 3) 저장
            userClusterRepository.save(userCluster);

            // 4) 사용자 엔티티에도 연결
            user.setUserCluster(userCluster);
            userRepository.save(user);
        }

        // ---------------------------
        // 2) Couple assignments 저장
        // ---------------------------
        File coupleAssignmentsFile = new File(jsonOutputDir, "couple_assignments.json");
        List<Map<String, Object>> coupleAssignments = objectMapper.readValue(
            coupleAssignmentsFile,
            new TypeReference<List<Map<String, Object>>>() {}
        );

        for (Map<String, Object> assignment : coupleAssignments) {
            Integer coupleId = ((Number) assignment.get("coupleid")).intValue();
            Integer clusterId = ((Number) assignment.get("cluster")).intValue();

            Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));

            // 1) 기존 CoupleCluster 있으면 재사용, 없으면 새로 생성
            CoupleCluster coupleCluster = couple.getCoupleCluster();
            if (coupleCluster == null) {
                coupleCluster = CoupleCluster.builder()
                    .couple(couple)
                    .build();
            }
            // 2) 클러스터 ID를 매번 새로 덮어쓰기
            coupleCluster.setClusterId(clusterId);

            // weighted_features 배열에서 좌표값 추출
            List<Double> weightedFeatures = (List<Double>) assignment.get("weighted_features");
            coupleCluster.setMaleAgeCoordinate(weightedFeatures.get(0));
            coupleCluster.setFemaleAgeCoordinate(weightedFeatures.get(1));
            coupleCluster.setAgeDiffCoordinate(weightedFeatures.get(2));
            coupleCluster.setBudgetCoordinate(weightedFeatures.get(3));
            coupleCluster.setMarriageMonthSinCoordinate(weightedFeatures.get(4));
            coupleCluster.setMarriageMonthCosCoordinate(weightedFeatures.get(5));
            coupleCluster.setMarriageDaySinCoordinate(weightedFeatures.get(6));
            coupleCluster.setMarriageDayCosCoordinate(weightedFeatures.get(7));
            coupleCluster.setUserClusterDistanceCoordinate(weightedFeatures.get(8));

            // 3) 저장
            coupleClusterRepository.save(coupleCluster);

            // 4) 커플 엔티티에도 연결
            couple.setCoupleCluster(coupleCluster);
            coupleRepository.save(couple);
        }
    }

    /**
     * 클러스터링 파라미터 정보를 DB에 저장합니다.
     * User와 Couple 각각의 클러스터 중심점과 가중치 정보를 저장합니다.
     */
    private void saveClusteringParameters() throws IOException {
        saveUserClusteringParameters();
        saveCoupleClusteringParameters();
    }

    /**
     * User 클러스터링 파라미터를 저장합니다.
     * 클러스터 중심점과 최적화된 가중치 값을 저장합니다.
     */
    private void saveUserClusteringParameters() throws IOException {
        // User cluster centers 저장
        File userCentersFile = new File(jsonOutputDir, "user_cluster_centers.json");
        Map<String, Map<String, Double>> userCenters = objectMapper.readValue(
            userCentersFile,
            new TypeReference<Map<String, Map<String, Double>>>() {}
        );

        UserClusterCenters centers = new UserClusterCenters();
        for (Map.Entry<String, Map<String, Double>> entry : userCenters.entrySet()) {
            // 예: key = "cluster_0"
            String key = entry.getKey();
            int clusterId = Integer.parseInt(key.replace("cluster_", ""));
            
            Map<String, Double> coordinates = entry.getValue();

            // 클러스터 ID를 꼭 지정해야 DB에 null이 아닌 값이 저장됨
            UserClusterCenter center = UserClusterCenter.builder()
                .clusterId(clusterId)
                .ageCoordinate(coordinates.get("age"))
                .budgetCoordinate(coordinates.get("budget"))
                .mbtiIeCoordinate(coordinates.get("mbti_ie"))
                .mbtiSnCoordinate(coordinates.get("mbti_sn"))
                .mbtiTfCoordinate(coordinates.get("mbti_tf"))
                .mbtiJpCoordinate(coordinates.get("mbti_jp"))
                .build();
            
            centers.addUserClusterCenter(center);
        }
        userClusterCentersRepository.save(centers);

        // User scaler parameters 저장
        File userScalerParamsFile = new File(jsonOutputDir, "user_scaler_params.json");
        Map<String, Map<String, List<Double>>> scalerParams = objectMapper.readValue(
            userScalerParamsFile,
            new TypeReference<Map<String, Map<String, List<Double>>>>() {}
        );

        UserClusteringParameter parameter = UserClusteringParameter.builder()
            // age 스케일러 파라미터
            .ageMean(scalerParams.get("age").get("mean_").get(0))
            .ageVar(scalerParams.get("age").get("var_").get(0))
            .ageScale(scalerParams.get("age").get("scale_").get(0))
            
            // budget 스케일러 파라미터
            .budgetScale(scalerParams.get("budget").get("scale_").get(0))
            .budgetMin(scalerParams.get("budget").get("min_").get(0))
            .budgetDataMin(scalerParams.get("budget").get("data_min_").get(0))
            .budgetDataMax(scalerParams.get("budget").get("data_max_").get(0))
            .build();

        // 가중치 파일에서 로드
        File userParamsFile = new File(jsonOutputDir, "user_optimized_weights.json");
        Map<String, List<Double>> weights = objectMapper.readValue(
            userParamsFile,
            new TypeReference<Map<String, List<Double>>>() {}
        );

        List<Double> weightsList = weights.get("weights");
        parameter.setAgeWeight(weightsList.get(0));
        parameter.setBudgetWeight(weightsList.get(1));
        parameter.setMbtiIeWeight(weightsList.get(2));
        parameter.setMbtiSnWeight(weightsList.get(3));
        parameter.setMbtiTfWeight(weightsList.get(4));
        parameter.setMbtiJpWeight(weightsList.get(5));

        userClusteringParameterRepository.save(parameter);
    }

    /**
     * Couple 클러스터링 파라미터를 저장합니다.
     * 클러스터 중심점과 최적화된 가중치 값을 저장합니다.
     */
    private void saveCoupleClusteringParameters() throws IOException {
        // Couple cluster centers 저장
        File coupleCentersFile = new File(jsonOutputDir, "couple_cluster_centers.json");
        Map<String, Map<String, Double>> coupleCenters = objectMapper.readValue(
            coupleCentersFile,
            new TypeReference<Map<String, Map<String, Double>>>() {}
        );

        // CoupleClusterCenters 생성 및 저장
        CoupleClusterCenters centers = new CoupleClusterCenters();
        for (Map.Entry<String, Map<String, Double>> entry : coupleCenters.entrySet()) {
            String key = entry.getKey();
            int clusterId = Integer.parseInt(key.replace("cluster_", ""));
            Map<String, Double> coordinates = entry.getValue();

            CoupleClusterCenter center = CoupleClusterCenter.builder()
                .clusterId(clusterId)
                .maleAgeCoordinate(coordinates.get("male_age"))
                .femaleAgeCoordinate(coordinates.get("female_age"))
                .ageDiffCoordinate(coordinates.get("age_diff"))
                .budgetCoordinate(coordinates.get("budget"))
                .marriageMonthSinCoordinate(coordinates.get("marriage_month_sin"))
                .marriageMonthCosCoordinate(coordinates.get("marriage_month_cos"))
                .marriageDaySinCoordinate(coordinates.get("marriage_day_sin"))
                .marriageDayCosCoordinate(coordinates.get("marriage_day_cos"))
                .userClusterDistanceCoordinate(coordinates.get("user_cluster_distance"))
                .build();

            centers.addCoupleClusterCenter(center);
        }
        coupleClusterCentersRepository.save(centers);

        // Couple scaler parameters 저장
        File coupleScalerParamsFile = new File(jsonOutputDir, "couple_scaler_params.json");
        Map<String, Map<String, List<Double>>> scalerParams = objectMapper.readValue(
            coupleScalerParamsFile,
            new TypeReference<Map<String, Map<String, List<Double>>>>() {}
        );

        CoupleClusteringParameter parameter = CoupleClusteringParameter.builder()
            // male_age
            .maleAgeMean(scalerParams.get("male_age").get("mean_").get(0))
            .maleAgeVar(scalerParams.get("male_age").get("var_").get(0))
            .maleAgeScale(scalerParams.get("male_age").get("scale_").get(0))
            
            // female_age
            .femaleAgeMean(scalerParams.get("female_age").get("mean_").get(0))
            .femaleAgeVar(scalerParams.get("female_age").get("var_").get(0))
            .femaleAgeScale(scalerParams.get("female_age").get("scale_").get(0))
            
            // age_diff
            .ageDiffScale(scalerParams.get("age_diff").get("scale_").get(0))
            .ageDiffMin(scalerParams.get("age_diff").get("min_").get(0))
            .ageDiffDataMin(scalerParams.get("age_diff").get("data_min_").get(0))
            .ageDiffDataMax(scalerParams.get("age_diff").get("data_max_").get(0))
            
            // budget
            .budgetScale(scalerParams.get("budget").get("scale_").get(0))
            .budgetMin(scalerParams.get("budget").get("min_").get(0))
            .budgetDataMin(scalerParams.get("budget").get("data_min_").get(0))
            .budgetDataMax(scalerParams.get("budget").get("data_max_").get(0))
            
            // marriage_month_sin/cos
            .marriageMonthSinScale(scalerParams.get("marriage_month_sin").get("scale_").get(0))
            .marriageMonthSinMin(scalerParams.get("marriage_month_sin").get("min_").get(0))
            .marriageMonthSinDataMin(scalerParams.get("marriage_month_sin").get("data_min_").get(0))
            .marriageMonthSinDataMax(scalerParams.get("marriage_month_sin").get("data_max_").get(0))
            
            .marriageMonthCosScale(scalerParams.get("marriage_month_cos").get("scale_").get(0))
            .marriageMonthCosMin(scalerParams.get("marriage_month_cos").get("min_").get(0))
            .marriageMonthCosDataMin(scalerParams.get("marriage_month_cos").get("data_min_").get(0))
            .marriageMonthCosDataMax(scalerParams.get("marriage_month_cos").get("data_max_").get(0))
            
            .marriageDaySinScale(scalerParams.get("marriage_day_sin").get("scale_").get(0))
            .marriageDaySinMin(scalerParams.get("marriage_day_sin").get("min_").get(0))
            .marriageDaySinDataMin(scalerParams.get("marriage_day_sin").get("data_min_").get(0))
            .marriageDaySinDataMax(scalerParams.get("marriage_day_sin").get("data_max_").get(0))
            
            .marriageDayCosScale(scalerParams.get("marriage_day_cos").get("scale_").get(0))
            .marriageDayCosMin(scalerParams.get("marriage_day_cos").get("min_").get(0))
            .marriageDayCosDataMin(scalerParams.get("marriage_day_cos").get("data_min_").get(0))
            .marriageDayCosDataMax(scalerParams.get("marriage_day_cos").get("data_max_").get(0))
            
            // user_cluster_distance
            .userClusterDistScale(scalerParams.get("user_cluster_distance").get("scale_").get(0))
            .userClusterDistMin(scalerParams.get("user_cluster_distance").get("min_").get(0))
            .userClusterDistDataMin(scalerParams.get("user_cluster_distance").get("data_min_").get(0))
            .userClusterDistDataMax(scalerParams.get("user_cluster_distance").get("data_max_").get(0))
            .build();

        // 가중치는 별도 파일에서 로드
        File coupleParamsFile = new File(jsonOutputDir, "couple_optimized_weights.json");
        Map<String, List<Double>> weights = objectMapper.readValue(
            coupleParamsFile,
            new TypeReference<Map<String, List<Double>>>() {}
        );

        List<Double> weightsList = weights.get("weights");
        parameter.setMaleAgeWeight(weightsList.get(0));
        parameter.setFemaleAgeWeight(weightsList.get(1));
        parameter.setAgeDiffWeight(weightsList.get(2));
        parameter.setBudgetWeight(weightsList.get(3));
        parameter.setMarriageMonthSinWeight(weightsList.get(4));
        parameter.setMarriageMonthCosWeight(weightsList.get(5));
        parameter.setMarriageDaySinWeight(weightsList.get(6));
        parameter.setMarriageDayCosWeight(weightsList.get(7));
        parameter.setUserClusterDistanceWeight(weightsList.get(8));

        coupleClusteringParameterRepository.save(parameter);
    }

    // 유효성 검사 메서드
    private void validateClusteringFiles() {
        validateFile(new File(jsonOutputDir, "user_assignments.json"), "User assignments");
        validateFile(new File(jsonOutputDir, "user_optimized_weights.json"), "User weights");
        validateFile(new File(jsonOutputDir, "user_cluster_centers.json"), "User cluster centers");
        validateFile(new File(jsonOutputDir, "couple_assignments.json"), "Couple assignments");
        validateFile(new File(jsonOutputDir, "couple_cluster_centers.json"), "Couple cluster centers");
        validateFile(new File(jsonOutputDir, "couple_optimized_weights.json"), "Couple weights");
    }

    private void validateFile(File file, String purpose) {
        if (!file.exists()) {
            log.error("{} file not found: {}", purpose, file.getAbsolutePath());
            throw new ExceptionHandler(ErrorStatus.CLUSTERING_FILE_IO_ERROR);
        }
    }

    private UserDetail validateUserDetail(User user) {
        return Optional.ofNullable(user.getUserDetail())
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.CLUSTERING_USER_DETAIL_NOT_FOUND));
    }

    private String validateMbti(String mbti) {
        if (mbti == null) return null;
        
        String upperMbti = mbti.toUpperCase();
        if (upperMbti.matches("^[EI][NS][TF][JP]$")) {
            return upperMbti;
        }
        return null;
    }
}