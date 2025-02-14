package com.ssafy.exhi.domain.todo.config;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.entity.StageType;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import com.ssafy.exhi.domain.todo.repository.TodoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TodoInitializer {

    private final TodoRepository todoRepository;

    // 캐싱용 Todo 리스트 (아직 DB에 저장 전, 메모리에만 존재)
    private final List<Todo> cachedTodos = new ArrayList<>();

    // 정의되지 않은 ServiceType/StageType 등에 대한 기본 투두
    private static final List<String> DEFAULT_TODOS = List.of(
            "1) 기본 투두 1",
            "2) 기본 투두 2",
            "3) 기본 투두 3"
    );

    /*
       ServiceType -> StageType -> (TodoType -> List<String>)
       구조의 사전 체크리스트 데이터.
       이 예시는 길이가 상당히 길므로, 실제 운영에서는 JSON/DB 등을 사용 권장
    */
    private static final Map<ServiceType, Map<StageType, Map<TodoType, List<String>>>> TODOS_DATA
            = createTodosData();

    /**
     * Spring Bean 생성 후 자동 실행되어,
     * 전체 사전 Todo 목록을 미리 cachedTodos에 생성해둔다.
     */
    @PostConstruct
    public void cacheDefaultTodos() {
        for (ServiceType serviceType : ServiceType.values()) {
            Map<StageType, Map<TodoType, List<String>>> stageMap = TODOS_DATA.get(serviceType);

            // 만약 정의되지 않았다면 PRE/ON/POST 모두 DEFAULT로 생성
            if (stageMap == null) {
                for (StageType stageType : StageType.values()) {
                    for (String content : DEFAULT_TODOS) {
                        for (TodoType todoType : TodoType.values()) {
                            cachedTodos.add(
                                    Todo.builder()
                                            .serviceType(serviceType)
                                            .stage(stageType)
                                            .todoType(todoType)
                                            .content(content)
                                            .isCompleted(false)
                                            .build()
                            );
                        }
                    }
                }
            } else {
                // 정의된 데이터가 있는 경우
                for (StageType stageType : StageType.values()) {
                    Map<TodoType, List<String>> todoTypeMap = stageMap.get(stageType);

                    if (todoTypeMap == null) {
                        // 해당 StageType이 미정의라면 DEFAULT
                        for (String content : DEFAULT_TODOS) {
                            for (TodoType todoType : TodoType.values()) {
                                cachedTodos.add(
                                        Todo.builder()
                                                .serviceType(serviceType)
                                                .stage(stageType)
                                                .todoType(todoType)
                                                .content(content)
                                                .isCompleted(false)
                                                .build()
                                );
                            }
                        }
                    } else {
                        // TodoType별로 실제 리스트 가져오기
                        for (TodoType todoType : TodoType.values()) {
                            List<String> contents = todoTypeMap.getOrDefault(todoType, DEFAULT_TODOS);
                            for (String content : contents) {
                                cachedTodos.add(
                                        Todo.builder()
                                                .serviceType(serviceType)
                                                .stage(stageType)
                                                .todoType(todoType)
                                                .content(content)
                                                .isCompleted(false)
                                                .build()
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 특정 Plan을 입력받아, plan.getServiceType()과 동일한 Todo들만 복사해서 DB에 저장
     */
    public void initializeTodosForPlan(Plan plan) {
        if (plan.getServiceType() == null) return;

        // 해당 ServiceType에 맞는 Todo만 필터링
        List<Todo> relevantTodos = cachedTodos.stream()
                .filter(todo -> todo.getServiceType() == plan.getServiceType())
                .collect(Collectors.toList());

        // 필터링된 Todo들을 Plan에 귀속시키면서 저장
        for (Todo cachedTodo : relevantTodos) {
            Todo newTodo = Todo.builder()
                    .plan(plan)
                    .serviceType(cachedTodo.getServiceType())
                    .stage(cachedTodo.getStage())
                    .todoType(cachedTodo.getTodoType())
                    .content(cachedTodo.getContent())
                    .isCompleted(false)
                    .build();
            plan.getTodos().add(newTodo);
            todoRepository.save(newTodo);
        }
    }

    /**
     * 하드코딩된 사전 체크리스트를 생성하는 정적 메서드
     * 실제론 파일/DB/관리도구 등에 분리 권장
     */
    private static Map<ServiceType, Map<StageType, Map<TodoType, List<String>>>> createTodosData() {
        Map<ServiceType, Map<StageType, Map<TodoType, List<String>>>> data = new HashMap<>();

        // ──────────────────────────────────────────
        // 1. WEDDING_HALL (웨딩홀 예약)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> weddingHallStages = new HashMap<>();

            // PRE
            weddingHallStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 예산 계획 수립 및 신랑 예산 검토",
                            "2) 웨딩홀 위치, 주차 등 접근성 조사",
                            "3) 후보 웨딩홀 목록 작성 (남편 의견 포함)"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 웨딩홀 인테리어 및 분위기 조사",
                            "2) 사진·동영상 자료로 스타일 비교",
                            "3) 원하는 컨셉과 장식 아이디어 정리"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 웨딩홀 상담 예약 (전화/방문)",
                            "2) 후보 리스트 최종 검토 및 비교",
                            "3) 계약 조건 및 할인 혜택 조사"
                    )
            ));

            // ON
            weddingHallStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 계약서 및 예산 항목 점검",
                            "2) 부대시설(주차·접근성) 최종 확인",
                            "3) 계약 조건·금액 협의"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 내부 분위기·장식 확인",
                            "2) 상담 중 궁금한 사항 메모",
                            "3) 샘플 메뉴 및 서비스 비교"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 웨딩홀 투어 및 상담 진행",
                            "2) 계약 조건 최종 협의",
                            "3) 예약금 결제 진행"
                    )
            ));

            // POST
            weddingHallStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 계약서 서류 정리 및 보관",
                            "2) 추가 비용 항목 재점검",
                            "3) 후속 미팅 일정(신랑) 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 웨딩홀 관련 사진·자료 정리",
                            "2) 디자인 및 세팅 아이디어 공유",
                            "3) 추가 문의사항 정리"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 계약 조건 재확인 및 검토",
                            "2) 후속 미팅·추가 상담 일정 예약",
                            "3) 최종 계약 확정 통보"
                    )
            ));

            data.put(ServiceType.WEDDING_HALL, weddingHallStages);
        }

        // ──────────────────────────────────────────
        // 2. STUDIO (스튜디오 촬영)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> studioStages = new HashMap<>();

            // PRE
            studioStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 촬영 예산 및 신랑 스타일 컨셉 조사",
                            "2) 후보 스튜디오 목록 작성",
                            "3) 촬영 아이디어(포즈·구도) 구상"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 촬영 컨셉·의상·메이크업 스타일 조사",
                            "2) 포트폴리오 및 샘플 사진 비교",
                            "3) 스튜디오 상담 예약 및 일정 조율"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 스튜디오 위치 및 접근성 확인",
                            "2) 촬영 패키지 및 옵션 비교",
                            "3) 촬영 일정·소요 시간 협의"
                    )
            ));

            // ON
            studioStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 스튜디오 내부 시설·장비 점검",
                            "2) 촬영 포즈·구도 공유 및 점검",
                            "3) 가격·조건 협의 (신랑 의견 반영)"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 의상·메이크업 시연 컨셉 확인",
                            "2) 촬영 분위기·배경 점검",
                            "3) 세부 촬영 컨셉·옵션 논의"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 스튜디오 상담 및 투어 진행",
                            "2) 최종 패키지·옵션 협의",
                            "3) 예약금 결제 및 촬영 일정 확정"
                    )
            ));

            // POST
            studioStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 촬영 계약서 검토 및 서류 정리",
                            "2) 추가 촬영 옵션 및 리마인더 설정",
                            "3) 후속 촬영 일정(신랑 측) 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 촬영 샘플·참고 자료 정리",
                            "2) 의상 및 스타일 피드백 기록",
                            "3) 추가 요청사항 정리"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 계약 조건 재검토 및 확정",
                            "2) 최종 촬영 일정·스케줄 조율",
                            "3) 계약 완료 확인 및 결제 완료"
                    )
            ));

            data.put(ServiceType.STUDIO, studioStages);
        }

        // ──────────────────────────────────────────
        // 3. DRESS_SHOP (드레스샵)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> dressShopStages = new HashMap<>();

            // PRE
            dressShopStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 신랑 예복 요구사항 및 예산 정리",
                            "2) 드레스샵 위치·접근성 조사",
                            "3) 드레스 디자인 트렌드(남편 의견) 조사"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 원하는 드레스 스타일·디자인 조사",
                            "2) 샘플 사진 및 참고 자료 수집",
                            "3) 쇼룸 방문 예약 및 상담 일정 조율"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 드레스샵 후보 목록 작성",
                            "2) 드레스 옵션·가격 비교",
                            "3) 리뷰·고객 평가 확인"
                    )
            ));

            // ON
            dressShopStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 방문 시 가격·조건 확인",
                            "2) 결제 방식 및 추가 비용 점검",
                            "3) 신랑 의견 전달 및 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 드레스 피팅·스타일 상담 진행",
                            "2) 드레스 핏·디자인 세부 확인",
                            "3) 메이크업·액세서리 연출 아이디어 논의"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 드레스샵 상담 및 투어 진행",
                            "2) 옵션 협의 및 예약금 결제",
                            "3) 계약 조건 최종 점검"
                    )
            ));

            // POST
            dressShopStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 계약서·결제 내역 정리·보관",
                            "2) 추가 비용·수정 사항 검토",
                            "3) 후속 상담 일정(신랑 측) 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 피팅 후 최종 드레스 선택 확정",
                            "2) 피드백·수정 요청 정리",
                            "3) 최종 계약 준비 및 서명"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 최종 계약 조건 재검토 및 확정",
                            "2) 드레스 계약 완료 확인",
                            "3) 관련 서류 정리 및 보관"
                    )
            ));

            data.put(ServiceType.DRESS_SHOP, dressShopStages);
        }

        // ──────────────────────────────────────────
        // 4. MAKEUP_STUDIO (메이크업 스튜디오)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> makeupStudioStages = new HashMap<>();

            // PRE
            makeupStudioStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 메이크업 예산 및 요구사항 정리",
                            "2) 후보 메이크업 스튜디오 조사",
                            "3) 상담 예약 및 일정 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 원하는 메이크업 스타일·트렌드 조사",
                            "2) 메이크업 포트폴리오·시연 자료 확인",
                            "3) 체험 예약 및 상담 일정 조율"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 서비스 옵션·가격 비교",
                            "2) 스튜디오 위치·시설 조사",
                            "3) 패키지 구성·옵션 정리"
                    )
            ));

            // ON
            makeupStudioStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 방문 시 남성 스타일 컨셉 확인",
                            "2) 추가 옵션·비용 점검",
                            "3) 상담 중 의견 반영"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 메이크업 시연·스타일 테스트 진행",
                            "2) 아티스트와 세부 사항 논의",
                            "3) 체험 후 피드백 제공"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 상담 진행 및 서비스 옵션 확정",
                            "2) 예약금 및 계약 조건 협의",
                            "3) 상담 결과 정리 및 기록"
                    )
            ));

            // POST
            makeupStudioStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 상담 후 서비스 내용 재검토",
                            "2) 계약서·결제 내역 확인",
                            "3) 후속 상담·추가 서비스 일정 점검"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 체험 후 스타일 최종 결정",
                            "2) 추가 요청·수정 사항 정리",
                            "3) 계약 조건 재검토 및 확정"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 계약 완료 및 서류 보관",
                            "2) 결과물 확인·피드백 수집",
                            "3) 후속 리허설 일정 조율"
                    )
            ));

            data.put(ServiceType.MAKEUP_STUDIO, makeupStudioStages);
        }

        // ──────────────────────────────────────────
        // 5. HANBOK (한복)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> hanbokStages = new HashMap<>();

            // PRE
            hanbokStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 신랑용 한복 스타일·디자인 조사",
                            "2) 대여·구매 예산 산정",
                            "3) 한복샵 후보 리스트 작성"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 한복 디자인·색상·패턴 조사",
                            "2) 치맛단·악세서리 옵션 비교",
                            "3) 한복샵 상담 예약·일정 조율"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 한복 옵션·가격 비교",
                            "2) 고객 리뷰·추천 업체 확인",
                            "3) 상담 일정 및 예약 확정"
                    )
            ));

            // ON
            hanbokStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 한복샵 방문, 제품 상태·디자인 확인",
                            "2) 사이즈 측정 및 피팅 진행",
                            "3) 계약 조건·추가 옵션 협의"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 한복 피팅 및 디자인 선택 진행",
                            "2) 스타일링·악세서리 조합 점검",
                            "3) 피팅 후 수정사항 논의"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 상담 및 피팅 진행",
                            "2) 제품 옵션·계약 조건 최종 협의",
                            "3) 예약금 결제·계약 진행"
                    )
            ));

            // POST
            hanbokStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 최종 계약서·피팅 결과 확인",
                            "2) 추가 수정·비용 검토",
                            "3) 후속 상담 일정(신랑 측) 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 피팅 후 최종 한복 선택 확정",
                            "2) 스타일링 피드백·수정 요청 정리",
                            "3) 계약 서류 확인 및 보관"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 최종 계약 조건 재검토 및 확정",
                            "2) 계약 완료·예약 확인",
                            "3) 관련 서류 정리 및 후속 일정 예약"
                    )
            ));

            data.put(ServiceType.HANBOK, hanbokStages);
        }

        // ──────────────────────────────────────────
        // 6. TAILOR_SHOP (남성 예복)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> tailorShopStages = new HashMap<>();

            // PRE
            tailorShopStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 남성 예복 스타일·디자인 조사",
                            "2) 맞춤 제작 예산 산정",
                            "3) 타일러샵 후보·추천 업체 조사"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 신랑 예복 트렌드·스타일 조사",
                            "2) 디자인·피팅 참고 자료 수집",
                            "3) 상담 예약·방문 일정 조율"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 예복 옵션·가격 비교",
                            "2) 고객 리뷰·추천 업체 확인",
                            "3) 상담·견적 예약 진행"
                    )
            ));

            // ON
            tailorShopStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 타일러샵 방문, 예복 디자인·소재 확인",
                            "2) 사이즈 측정·피팅 진행",
                            "3) 가격·제작 기간 협의"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 상담 시 신랑 스타일·매칭 포인트 점검",
                            "2) 디자인·액세서리 조합 확인",
                            "3) 피팅 후 의견 전달·조정"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 상담 및 피팅 진행",
                            "2) 최종 예복 옵션·가격 협의",
                            "3) 예약금·계약 조건 확정"
                    )
            ));

            // POST
            tailorShopStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 예복 제작 계약서·피팅 결과 확인",
                            "2) 추가 수정·비용 검토",
                            "3) 후속 상담·제작 일정 점검"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 피팅 후 최종 예복 디자인 확정",
                            "2) 디자인 변경·액세서리 추가 협의",
                            "3) 계약 내용·결제 내역 정리"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 최종 계약 조건 재확인·확정",
                            "2) 제작 완료·수령 일정 예약",
                            "3) 관련 서류 정리 및 보관"
                    )
            ));

            data.put(ServiceType.TAILOR_SHOP, tailorShopStages);
        }

        // ──────────────────────────────────────────
        // 7. SNAP (스냅 촬영)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> snapStages = new HashMap<>();

            // PRE
            snapStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 촬영 컨셉·포즈 아이디어 구상(신랑 스타일 고려)",
                            "2) 사진작가·스냅 촬영 업체 후보 리스트업",
                            "3) 촬영 예산·일정 검토"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 촬영 컨셉·분위기 조사",
                            "2) 포트폴리오·샘플 사진 비교",
                            "3) 촬영 스타일·의상 참고 자료 수집"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 촬영 업체·패키지 옵션 비교",
                            "2) 상담 예약·촬영 일정 조율",
                            "3) 촬영 장소·소품 준비 사항 확인"
                    )
            ));

            // ON
            snapStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 촬영 진행 시 포즈·스타일 점검",
                            "2) 남성 이미지 강조 촬영 확인",
                            "3) 촬영 조건·비용 협의 (신랑 의견 반영)"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 다양한 포즈·표정 연출",
                            "2) 의상·헤어 스타일 점검",
                            "3) 촬영 진행 상황·피드백 공유"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 촬영 현장·투어 진행",
                            "2) 최종 촬영 일정·조건 협의",
                            "3) 예약금·계약 조건 확정"
                    )
            ));

            // POST
            snapStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 촬영 결과물·수정 요청 검토",
                            "2) 사진 인화·보관 방법 확인",
                            "3) 추가 촬영 옵션·후속 일정 점검"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 촬영 결과물 확인·보정 요청",
                            "2) 인화·앨범 구성 아이디어 논의",
                            "3) 피드백·수정 사항 정리"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 최종 결과물 검토·수정 요청",
                            "2) 피드백 수집·리뷰 작성",
                            "3) 결과물 보관·앨범 제작 일정 조율"
                    )
            ));

            data.put(ServiceType.SNAP, snapStages);
        }

        // ──────────────────────────────────────────
        // 8. INVITATION (청첩장 발송)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> invitationStages = new HashMap<>();

            // PRE
            invitationStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 청첩장 예산·스타일 요구사항 정리(신랑 의견)",
                            "2) 디자인 트렌드·서체 조사",
                            "3) 인쇄 업체·배송 옵션 조사"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 청첩장 디자인·레이아웃 조사",
                            "2) 초대 명단 정리·최종 확인",
                            "3) 샘플 청첩장 비교·선호 디자인 선정"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 청첩장 업체 후보 리스트업",
                            "2) 디자인·가격 비교",
                            "3) 발송 일정·방법 협의"
                    )
            ));

            // ON
            invitationStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 최종 예산 점검·계약 조건 확인",
                            "2) 할인 혜택·추가 옵션 점검",
                            "3) 신랑 의견 반영한 디자인 최종 확정"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 최종 디자인 선택·주문 진행",
                            "2) 내용·레이아웃 최종 검토",
                            "3) 시안 재확인 후 주문 확정"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 청첩장 최종 주문 확정",
                            "2) 발송 일정·방법 확정",
                            "3) 주문 확인·예약금 결제"
                    )
            ));

            // POST
            invitationStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 주문 내역·계약서 보관",
                            "2) 추가 인쇄 옵션·수정 사항 점검",
                            "3) 신랑 의견에 따른 최종 수정 검토"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 샘플 확인·최종 수정 요청",
                            "2) 배송 일정·추가 주문 여부 점검",
                            "3) 주문 확인 및 수정 사항 정리"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 청첩장 발송 완료 확인",
                            "2) 추가 주문 필요 여부 점검",
                            "3) 피드백·수정 사항 반영"
                    )
            ));

            data.put(ServiceType.INVITATION, invitationStages);
        }

        // ──────────────────────────────────────────
        // 9. FACIAL_CARE (신부 피부관리/케어)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> facialCareStages = new HashMap<>();

            // PRE
            facialCareStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 케어 프로그램 예산·옵션 조사(신랑 의견 참고)",
                            "2) 신부 피부 상태·요구사항 파악",
                            "3) 후보 케어 업체 리스트업"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 피부 관리 프로그램·트렌드 조사",
                            "2) 체험 후기·추천 업체 확인",
                            "3) 상담 예약·체험 일정 조율"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 케어 서비스 옵션·가격 비교",
                            "2) 업체 리뷰·평점 확인",
                            "3) 상담 및 예약 일정 조율"
                    )
            ));

            // ON
            facialCareStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 신부 피부 상태 체크·상담 참여",
                            "2) 추가 옵션·비용 점검",
                            "3) 상담 중 남편 의견 반영"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 피부 케어 시술 진행·효과 확인",
                            "2) 메이크업 리허설과 연계 점검",
                            "3) 시술 후 즉각 피드백 제공"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 케어 상담 및 시연 진행",
                            "2) 서비스 옵션 최종 확정",
                            "3) 예약금·계약 조건 협의"
                    )
            ));

            // POST
            facialCareStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 시술 결과·효과 검토",
                            "2) 추가 케어 필요 여부 논의",
                            "3) 후속 상담·추가 서비스 일정 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 시술 후 피부 상태 재점검",
                            "2) 개선 사항·추가 요청 정리",
                            "3) 후속 케어 예약·일정 조율"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 서비스 결과 확인·피드백 수집",
                            "2) 계약 조건·추가 옵션 재검토",
                            "3) 후속 관리 일정·리뷰 작성"
                    )
            ));

            data.put(ServiceType.FACIAL_CARE, facialCareStages);
        }

        // ──────────────────────────────────────────
        // 10. HONEYMOON (신혼여행)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> honeymoonStages = new HashMap<>();

            // PRE
            honeymoonStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 신혼여행 예산·목적지 후보 조사(신랑 의견)",
                            "2) 여행사·패키지 옵션 비교",
                            "3) 여행 일정·기간 협의"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 신혼여행 목적지·컨셉 조사",
                            "2) 여행지 분위기·후기 비교",
                            "3) 희망 일정·액티비티 조사"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 여행사·패키지 옵션 비교",
                            "2) 여행 일정·비용 산정",
                            "3) 예약 상담·일정 조율"
                    )
            ));

            // ON
            honeymoonStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 여행 상담·패키지 옵션 확인(신랑 의견 반영)",
                            "2) 항공권·숙소 예약 조건 점검",
                            "3) 비용·할인 혜택 협의"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 여행 세부 일정·활동 계획 점검",
                            "2) 여행사와 컨셉 세부 논의",
                            "3) 예약금·결제 조건 협의"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 상담 진행·여행 패키지 확정",
                            "2) 예약금 결제·일정 확정",
                            "3) 계약 조건·일정 재확인"
                    )
            ));

            // POST
            honeymoonStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 여행 예약 내역·계약서 정리",
                            "2) 추가 옵션·변경 사항 확인",
                            "3) 후속 상담·여행 일정 점검"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 최종 여행 일정 확정·피드백 정리",
                            "2) 추가 요청·변경 사항 정리",
                            "3) 예약 후 확인서·서류 점검"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 최종 예약 내용·계약 조건 재확인",
                            "2) 예약 완료·결제 내역 보관",
                            "3) 여행 전 최종 체크리스트 재검토"
                    )
            ));

            data.put(ServiceType.HONEYMOON, honeymoonStages);
        }

        // ──────────────────────────────────────────
        // 11. WEDDING_DAY (결혼식 당일)
        // ──────────────────────────────────────────
        {
            Map<StageType, Map<TodoType, List<String>>> weddingDayStages = new HashMap<>();

            // PRE
            weddingDayStages.put(StageType.PRE, Map.of(
                    TodoType.MALE, List.of(
                            "1) 최종 리허설 준비·예산 점검(신랑 체크)",
                            "2) 담당 스태프·차량·리무진 확인",
                            "3) 비상 연락망·역할 분담 확인"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 웨딩 드레스·메이크업 최종 점검",
                            "2) 장식·꽃·악세서리 최종 확인",
                            "3) 리허설·스케줄 재확인"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 최종 리허설 및 전체 점검",
                            "2) 결혼식 당일 일정 최종 공유",
                            "3) 비상 상황 대응 계획 확인"
                    )
            ));

            // ON
            weddingDayStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "1) 결혼식 당일 신랑 역할 수행(사회자 협의)",
                            "2) 현장 진행·문제 발생 시 즉각 대응",
                            "3) 신랑 스케줄·현장 관리"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 결혼식 당일 신부 역할 수행·세부 점검",
                            "2) 현장 장식·디테일 관리",
                            "3) 하객 응대·문제 해결"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 전체 진행 상황 실시간 체크",
                            "2) 사진/영상 촬영·스케줄 관리",
                            "3) 양측 협력하여 문제 해결"
                    )
            ));

            // POST
            weddingDayStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "1) 결혼식 후 정산·후속 비용 확인",
                            "2) 신랑 관련 피드백·개선 사항 정리",
                            "3) 후속 미팅·감사 인사 준비"
                    ),
                    TodoType.FEMALE, List.of(
                            "1) 결혼식 후 신부 관련 피드백·정리",
                            "2) 감사 메시지·후기 작성",
                            "3) 추가 요청·수정 사항 정리"
                    ),
                    TodoType.COMMON, List.of(
                            "1) 결혼식 결과 정리·계약서/결제 내역 보관",
                            "2) 후속 감사 인사·리뷰 작성",
                            "3) 결혼식 관련 자료 정리·보관"
                    )
            ));

            data.put(ServiceType.WEDDING_DAY, weddingDayStages);
        }

        return data;
    }
}