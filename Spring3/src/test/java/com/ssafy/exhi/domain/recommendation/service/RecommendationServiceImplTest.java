package com.ssafy.exhi.domain.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exhi.domain.recommendation.model.dto.RecommendationResponse;
import com.ssafy.exhi.domain.recommendation.model.entity.*;
import com.ssafy.exhi.domain.recommendation.repository.*;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Mock
    private UserClusterRepository userClusterRepository;
    @Mock
    private ClusteringParameterRepository clusteringParameterRepository;
    @Mock
    private ClusterCentersRepository clusterCentersRepository;
    @Mock
    private ClusterRepresentativeRepository representativeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Environment env;

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(env.getProperty(eq("json.output.dir"))).thenReturn(tempDir.toString());
        when(env.getProperty(eq("python.interpreter"))).thenReturn("python");
        when(env.getProperty(eq("python.script.dir"))).thenReturn(tempDir.toString());
        when(env.getProperty(eq("python.script.name"))).thenReturn("clustering.py");
        when(env.getProperty(eq("python.script.timeout"), anyString())).thenReturn("30");

        recommendationService.init();
    }

    @Nested
    @DisplayName("1. generateUserDataJSON() 테스트")
    class GenerateUserDataJSONTest {
        @Test
        @DisplayName("정상적으로 input_data.json 파일을 생성한다")
        void generateUserDataJSON_Success() throws IOException {
            // given
            List<User> fakeUsers = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                User user = new User();
                user.setId(i+1);

                UserDetail detail = new UserDetail();
                detail.setAge(20+i);
                detail.setMbti("INTP");
                detail.setBudget(1000000 + i*100000);
                user.setUserDetail(detail);

                fakeUsers.add(user);
            }
            when(userRepository.findAll()).thenReturn(fakeUsers);

            File expectedFile = tempDir.resolve("input_data.json").toFile();

            // when
            recommendationService.generateUserDataJSON();

            // then
            assertTrue(expectedFile.exists(), "input_data.json 파일이 생성되어 있어야 함");

            List<Map<String, Object>> userData = 
                objectMapper.readValue(expectedFile, new TypeReference<>() {});
            assertEquals(3, userData.size());

            Map<String, Object> firstUserMap = userData.get(0);
            assertEquals(20, firstUserMap.get("age"));
            assertEquals("INTP", firstUserMap.get("mbti"));
            assertEquals(1000000, firstUserMap.get("budget"));
            assertTrue((Boolean) firstUserMap.get("routine_completed"));
        }
    }

    @Nested
    @DisplayName("2. executeClusteringProcess() 테스트")
    class ExecuteClusteringProcessTest {
        @Test
        @DisplayName("정상적으로 파이썬 스크립트를 호출하는 경우")
        void executeClusteringProcess_Success() throws IOException {
            // given
            // input_data.json 파일 생성
            List<Map<String, Object>> testData = new ArrayList<>();
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", 1);
            userData.put("age", 28);
            userData.put("budget", 5000);
            userData.put("mbti", "INTJ");
            userData.put("routine_completed", true);
            testData.add(userData);
            
            File inputFile = tempDir.resolve("input_data.json").toFile();
            objectMapper.writeValue(inputFile, testData);

            // Python 스크립트 파일도 생성
            File scriptFile = tempDir.resolve("clustering.py").toFile();
            // 간단한 더미 스크립트 생성
            Files.write(scriptFile.toPath(), "print('test')\n".getBytes());

            // when & then
            assertTrue(inputFile.exists(), "input_data.json 파일이 존재해야 함");
            assertDoesNotThrow(() -> recommendationService.executeClusteringProcess());
        }

        @Test
        @DisplayName("input_data.json 파일이 없는 경우")
        void executeClusteringProcess_NoInputFile() {
            // given
            File inputFile = tempDir.resolve("input_data.json").toFile();
            assertFalse(inputFile.exists(), "input_data.json 파일이 없어야 함");

            // when & then
            assertThrows(ExceptionHandler.class,
                () -> recommendationService.executeClusteringProcess());
        }
    }

    @Nested
    @DisplayName("3. processClusteringResults() 테스트")
    class ProcessClusteringResultsTest {
        @Test
        @DisplayName("정상적으로 JSON 파일들을 읽고 DB에 저장한다")
        void processClusteringResults_Success() throws IOException {
            // given
            createTestJsonFiles();

            User fakeUser = new User();
            fakeUser.setId(1);
            when(userRepository.findById(1)).thenReturn(Optional.of(fakeUser));

            // when
            recommendationService.processClusteringResults();

            // then
            verify(clusteringParameterRepository, times(1)).save(any(ClusteringParameter.class));
            verify(clusterCentersRepository, times(2)).save(any(ClusterCenters.class));
            verify(representativeRepository, times(1)).deleteAll();
            verify(userClusterRepository, times(1)).deleteAll();
            verify(userClusterRepository, times(1)).save(any(UserCluster.class));
            verify(representativeRepository, times(1)).save(any(ClusterRepresentative.class));
        }

        private void createTestJsonFiles() throws IOException {
            // scalers.json
            Map<String, Object> scalers = new HashMap<>();
            Map<String, Object> ageMap = new HashMap<>();
            ageMap.put("mean", 25.0);
            ageMap.put("scale", 5.0);
            scalers.put("age", ageMap);
            Map<String, Object> budgetMap = new HashMap<>();
            budgetMap.put("min", 1000000.0);
            budgetMap.put("max", 5000000.0);
            scalers.put("budget", budgetMap);
            objectMapper.writeValue(tempDir.resolve("scalers.json").toFile(), scalers);

            // optimized_weights.json
            Map<String, Object> weights = new HashMap<>();
            weights.put("age_weight", 0.5);
            weights.put("budget_weight", 0.3);
            weights.put("mbti_weights", List.of(0.1, 0.1, 0.1, 0.1));
            objectMapper.writeValue(tempDir.resolve("optimized_weights.json").toFile(), weights);

            // cluster_centers.json
            List<Map<String, Object>> centerList = List.of(
                Map.of(
                    "center_id", 0,
                    "coordinates", Map.of(
                        "age", 0.1,
                        "budget", 0.2,
                        "mbti", List.of(0.0, 1.0, 0.5, 0.3)
                    )
                )
            );
            objectMapper.writeValue(tempDir.resolve("cluster_centers.json").toFile(), centerList);

            // clustering_results.json
            Map<String, Object> resultsMap = Map.of(
                "user_clusters", List.of(Map.of("user_id", 1, "cluster_id", 0)),
                "cluster_representatives", List.of(Map.of("cluster_id", 0, "representative_id", 1))
            );
            objectMapper.writeValue(tempDir.resolve("clustering_results.json").toFile(), resultsMap);
        }
    }

    @Nested
    @DisplayName("4. addUserToCluster() 테스트")
    class AddUserToClusterTest {
        @Test
        @DisplayName("사용자 한 명을 클러스터에 할당하는 로직이 정상 동작한다")
        void addUserToCluster_Success() {
            // given
            User user = createTestUser();
            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            when(clusteringParameterRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(createTestParameter()));
            when(clusterCentersRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(createTestCenters()));

            // when
            recommendationService.addUserToCluster(1);

            // then
            verify(userClusterRepository, times(1)).save(any(UserCluster.class));
        }

        private User createTestUser() {
            User user = new User();
            user.setId(1);
            UserDetail detail = new UserDetail();
            detail.setAge(25);
            detail.setBudget(3000000);
            detail.setMbti("INTP");
            user.setUserDetail(detail);
            return user;
        }

        private ClusteringParameter createTestParameter() {
            return ClusteringParameter.builder()
                .ageMean(25.0).ageScale(5.0)
                .budgetMin(1000000.0).budgetMax(5000000.0)
                .ageWeight(0.5).budgetWeight(0.3)
                .mbtiIeWeight(0.1).mbtiSnWeight(0.1)
                .mbtiTfWeight(0.1).mbtiJpWeight(0.1)
                .build();
        }

        private ClusterCenters createTestCenters() {
            ClusterCenters centers = new ClusterCenters();
            ClusterCenter center = ClusterCenter.builder()
                .clusterId(0)
                .ageCoordinate(0.0)
                .budgetCoordinate(0.0)
                .mbtiIeCoordinate(0.5)
                .mbtiSnCoordinate(0.5)
                .mbtiTfCoordinate(0.5)
                .mbtiJpCoordinate(0.5)
                .build();
            centers.addClusterCenter(center);
            return centers;
        }
    }

    @Nested
    @DisplayName("5. getRecommendedPlans() 테스트")
    class GetRecommendedPlansTest {
        @Test
        @DisplayName("사용자 클러스터가 존재하는 경우, 추천 DTO를 반환한다")
        void getRecommendedPlans_Success() {
            // given
            User user = new User();
            user.setId(1);
            UserCluster userCluster = UserCluster.builder()
                .user(user)
                .clusterId(0)
                .build();

            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            when(userClusterRepository.findByUser(user))
                .thenReturn(Optional.of(userCluster));

            // when
            RecommendationResponse.RecommendedPlansDTO result = 
                recommendationService.getRecommendedPlans(1);

            // then
            assertNotNull(result);
        }

        @Test
        @DisplayName("사용자 클러스터 정보가 없으면 예외 발생")
        void getRecommendedPlans_NoCluster() {
            // given
            User user = new User();
            user.setId(1);
            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            when(userClusterRepository.findByUser(user)).thenReturn(Optional.empty());

            // when & then
            assertThrows(ExceptionHandler.class,
                () -> recommendationService.getRecommendedPlans(1));
        }
    }
}
