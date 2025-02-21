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
                            "웨딩홀 예산 상한선 설정하기",
                            "대중교통 접근성 확인하기",
                            "하객 주차 가능 대수 체크하기",
                            "웨딩홀 후보 3곳 이상 선정하기",
                            "양가 부모님 동행 가능 일정 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "웨딩홀 인스타그램/블로그 후기 수집하기",
                            "실제 웨딩 영상 3개 이상 찾아보기",
                            "선호하는 홀 장식 사진 저장하기",
                            "웨딩홀 방문 전 체크리스트 작성하기",
                            "웨딩 플래너 추천 목록 받아두기"
                    ),
                    TodoType.COMMON, List.of(
                            "웨딩홀 3곳 이상 상담 예약하기",
                            "예식 희망 날짜 3개 이상 정하기",
                            "하객 예상 인원 계산하기",
                            "식사 메뉴 선호도 조사하기",
                            "웨딩홀 방문 일정표 만들기"
                    )
            ));

            // ON
            weddingHallStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "계약서 필수 체크 항목 확인하기",
                            "예약금 납부 계획 세우기",
                            "주차 요원 배치 계획 확인하기",
                            "비상 연락망 저장하기",
                            "계약금 카드 할부 개월 수 정하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "웨딩홀 조명 상태 체크하기",
                            "포토존 위치 3곳 이상 확인하기",
                            "신부대기실 위치/상태 확인하기",
                            "식사 메뉴 시식 일정 잡기",
                            "테이블 세팅 샘플 사진 찍어두기"
                    ),
                    TodoType.COMMON, List.of(
                            "예식 진행 동선 직접 걸어보기",
                            "하객 식사 공간 수용력 확인하기",
                            "음향/영상 시설 테스트하기",
                            "계약서 필수 항목 꼼꼼히 읽기",
                            "특수 조건 계약서 별도 기재하기"
                    )
            ));

            // POST
            weddingHallStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "계약서 사본 스캔하여 저장하기",
                            "영수증 파일로 보관하기",
                            "잔금 납부 일정 캘린더에 등록하기",
                            "담당자 연락처 저장하기",
                            "주차 관련 추가 협의사항 메모하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "웨딩홀 답사 사진 폴더 정리하기",
                            "식사 메뉴 최종 선택지 정리하기",
                            "장식/조명 요청사항 문서화하기",
                            "플라워 데코레이션 견적 요청하기",
                            "웨딩홀 동영상 촬영 허가 확인하기"
                    ),
                    TodoType.COMMON, List.of(
                            "계약 세부사항 엑셀로 정리하기",
                            "2차 미팅 일정 캘린더에 등록하기",
                            "식사 인원 보증 날짜 체크하기",
                            "스케줄표에 예식 일정 표시하기",
                            "웨딩홀 SNS 계정 팔로우하기"
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
                            "선호하는 포즈 사진 5장 이상 저장하기",
                            "정장 피팅 일정 잡기",
                            "헤어 스타일링 예약하기",
                            "촬영용 구두 준비하기",
                            "피부 관리 일정 잡기"
                    ),
                    TodoType.FEMALE, List.of(
                            "스튜디오별 포트폴리오 비교표 만들기",
                            "드레스 샵과 촬영 일정 조율하기",
                            "헤어메이크업 견적 받아보기",
                            "액세서리 리스트 작성하기",
                            "네일 디자인 정하기"
                    ),
                    TodoType.COMMON, List.of(
                            "스튜디오 3곳 이상 견적 받기",
                            "촬영 가능 날짜 캘린더 체크하기",
                            "야외 촬영 장소 후보 정하기",
                            "비용 견적서 엑셀로 정리하기",
                            "상담 예약 전 질문리스트 만들기"
                    )
            ));

            // ON
            studioStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "촬영 당일 복장 및 소품 체크리스트 작성하기",
                            "원하는 포즈/구도 사진 촬영팀과 공유하기",
                            "예약금 결제 방식 확인하기",
                            "촬영 소요시간 체크하기",
                            "야외촬영 시 우천대비 계획 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "헤어메이크업 리허설 진행하기",
                            "드레스/소품 최종 피팅하기",
                            "촬영 스케줄표 시간대별 정리하기",
                            "원하는 사진 컨셉 레퍼런스 전달하기",
                            "메이크업 유지 용품 준비리스트 작성하기"
                    ),
                    TodoType.COMMON, List.of(
                            "스튜디오 계약서 세부 내용 확인하기",
                            "촬영 패키지 옵션 최종 선택하기",
                            "앨범 구성 방식 협의하기",
                            "추가 촬영 컷 가격 확인하기",
                            "보정 작업 범위 상세 조율하기"
                    )
            ));

            // POST
            studioStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "촬영본 수정 요청사항 정리하기",
                            "앨범 추가 구매 여부 결정하기",
                            "사진 파일 보관용 USB 준비하기",
                            "웨딩 영상 러닝타임 확인하기",
                            "결제 영수증 보관하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "보정 요청사항 상세히 작성하기",
                            "앨범 커버 디자인 선택하기",
                            "스냅사진 SNS 업로드 허가 여부 결정하기",
                            "본식 촬영 요청사항 정리하기",
                            "웨딩 앨범 구성 순서 정하기"
                    ),
                    TodoType.COMMON, List.of(
                            "촬영 원본 파일 백업하기",
                            "앨범 제작 완료일 확인하기",
                            "웨딩 영상 BGM 선곡하기",
                            "보정 완료 후 피드백 전달하기",
                            "후기 작성하기"
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
                            "정장 스타일 레퍼런스 5장 이상 수집하기",
                            "정장 대여/구매 예산 설정하기",
                            "피팅 가능한 드레스샵 리스트 작성하기",
                            "정장 사이즈 측정하기",
                            "액세서리 필요 항목 체크하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "드레스 스타일 북 만들기",
                            "체형에 맞는 드레스 라인 조사하기",
                            "드레스샵 3곳 이상 상담 예약하기",
                            "드레스 피팅 동행인 섭외하기",
                            "웨딩슈즈 후보 리스트 작성하기"
                    ),
                    TodoType.COMMON, List.of(
                            "드레스샵 견적 비교표 만들기",
                            "피팅 일정표 작성하기",
                            "드레스샵 위치/교통편 확인하기",
                            "예약금 준비하기",
                            "드레스샵 후기/리뷰 정리하기"
                    )
            ));

            // ON
            dressShopStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "정장 사이즈 세부 측정하기",
                            "셔츠/넥타이 조합 결정하기",
                            "구두 사이즈 맞춰보기",
                            "액세서리 착용 확인하기",
                            "대여 기간/가격 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "드레스 최소 3벌 이상 피팅하기",
                            "웨딩슈즈 굽높이 테스트하기",
                            "베일/티아라 스타일 결정하기",
                            "드레스 보정 포인트 체크하기",
                            "소품 및 악세서리 매칭해보기"
                    ),
                    TodoType.COMMON, List.of(
                            "패키지 구성 항목 꼼꼼히 체크하기",
                            "피팅 사진 촬영하고 저장하기",
                            "드레스 대여 기간 확정하기",
                            "추가 비용 항목 확인하기",
                            "계약서 필수 체크사항 검토하기"
                    )
            ));

            // POST
            dressShopStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "정장 수선 부위 체크리스트 작성하기",
                            "대여 기간 캘린더에 표시하기",
                            "액세서리 구매 목록 정리하기",
                            "피팅 사진 보관하기",
                            "수선 완료 후 최종 피팅 일정 잡기"
                    ),
                    TodoType.FEMALE, List.of(
                            "드레스 치수 보정 부위 상세 기록하기",
                            "드레스 픽업/반납 일정 캘린더에 등록하기",
                            "악세서리 구매 목록 엑셀로 정리하기",
                            "피팅 사진 폴더별로 정리하기",
                            "최종 피팅 일정 확정하기"
                    ),
                    TodoType.COMMON, List.of(
                            "계약서 스캔본 클라우드에 저장하기",
                            "드레스 수령/반납 체크리스트 만들기",
                            "추가 비용 영수증 정리하기",
                            "담당자 연락처 저장하기",
                            "드레스샵 이용 후기 작성하기"
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
                            "메이크업 예산 범위 설정하기",
                            "피부 관리 일정표 작성하기",
                            "메이크업 샵 3곳 이상 후보 선정하기",
                            "메이크업 전 피부과 상담 예약하기",
                            "메이크업 샵 위치/교통편 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "원하는 메이크업 스타일 사진 10장 이상 모으기",
                            "메이크업 아티스트 포트폴리오 비교하기",
                            "피부 타입별 주의사항 체크리스트 작성하기",
                            "헤어 스타일 레퍼런스 이미지 수집하기",
                            "메이크업 트라이얼 일정 잡기"
                    ),
                    TodoType.COMMON, List.of(
                            "메이크업 샵 견적 비교표 만들기",
                            "상담 예약 전 질문리스트 작성하기",
                            "결혼식 일정 기준 메이크업 타임라인 작성하기",
                            "메이크업 샵 후기/리뷰 정리하기",
                            "메이크업 전 피부 관리 계획 세우기"
                    )
            ));

            // ON
            makeupStudioStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "메이크업 전 피부 관리 체크리스트 확인하기",
                            "트라이얼 메이크업 사진 촬영하기",
                            "메이크업 지속력 테스트하기",
                            "피부 알레르기 반응 체크하기",
                            "메이크업 수정 사항 기록하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "트라이얼 메이크업 전/후 사진 찍기",
                            "헤어스타일 여러 가지 시도해보기",
                            "메이크업 지속력 체크하기",
                            "피부 트러블 여부 확인하기",
                            "메이크업/헤어 수정사항 상세 기록하기"
                    ),
                    TodoType.COMMON, List.of(
                            "메이크업 견적서 상세 내용 확인하기",
                            "예약금 납부하기",
                            "본식 메이크업 일정 확정하기",
                            "메이크업 담당자 연락처 저장하기",
                            "긴급상황 대비 백업 메이크업 샵 알아보기"
                    )
            ));

            // POST
            makeupStudioStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "메이크업 유지 제품 리스트 받기",
                            "메이크업 전/후 사진 저장하기",
                            "피부 관리 주의사항 메모하기",
                            "결제 내역 정리하기",
                            "본식 전 주의사항 체크리스트 만들기"
                    ),
                    TodoType.FEMALE, List.of(
                            "메이크업 완성 사진 여러 각도로 저장하기",
                            "메이크업 수정사항 문서화하기",
                            "본식 메이크업 요청사항 정리하기",
                            "메이크업 리텐션 제품 목록 작성하기",
                            "피부 관리 일정 캘린더에 등록하기"
                    ),
                    TodoType.COMMON, List.of(
                            "계약서 및 영수증 스캔하여 저장하기",
                            "본식 메이크업 일정표 만들기",
                            "메이크업 전 준비물 리스트 작성하기",
                            "메이크업 샵 동선 확인하기",
                            "메이크업 후기 작성하기"
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
                            "한복 대여/구매 예산 설정하기",
                            "선호하는 한복 색상 조합 정리하기",
                            "한복집 3곳 이상 후보 리스트 작성하기",
                            "한복 치수 측정하기",
                            "한복 대여 기간 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "한복 스타일 레퍼런스 10장 이상 수집하기",
                            "한복 업체 포트폴리오 비교하기",
                            "한복 액세서리 종류 조사하기",
                            "한복 피팅 동행인 섭외하기",
                            "한복 상담 예약 일정 잡기"
                    ),
                    TodoType.COMMON, List.of(
                            "한복집 위치 및 교통편 확인하기",
                            "한복 대여/구매 견적 비교표 만들기",
                            "한복집 실제 후기 검색하기",
                            "촬영 컨셉에 맞는 한복 스타일 정하기",
                            "예약금 준비하기"
                    )
            ));

            // ON
            hanbokStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "한복 치수 세부 측정하기",
                            "바지/저고리 색상 조합 결정하기",
                            "한복 신발 사이즈 확인하기",
                            "장신구 세트 구성 확인하기",
                            "대여 기간 및 비용 확정하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "한복 3벌 이상 피팅해보기",
                            "치마/저고리 색상 조합 결정하기",
                            "머리장식/노리개 선택하기",
                            "한복 신발 굽높이 테스트하기",
                            "치마길이 조절 포인트 체크하기"
                    ),
                    TodoType.COMMON, List.of(
                            "한복 대여/구매 계약서 검토하기",
                            "피팅 사진 여러 각도로 촬영하기",
                            "수선 필요 부위 체크하기",
                            "추가 옵션 항목 확인하기",
                            "한복 수령일/반납일 확정하기"
                    )
            ));

            // POST
            hanbokStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "한복 수선 부위 재확인하기",
                            "대여 기간 캘린더에 표시하기",
                            "장신구 세트 구성품 리스트 작성하기",
                            "착용법 메모하기",
                            "수선 완료 후 최종 피팅 일정 잡기"
                    ),
                    TodoType.FEMALE, List.of(
                            "한복 치마/저고리 길이 수정사항 기록하기",
                            "한복 수령/반납 일정 캘린더에 등록하기",
                            "액세서리 세트 구성품 체크리스트 만들기",
                            "한복 보관 방법 메모하기",
                            "최종 피팅 일정 확정하기"
                    ),
                    TodoType.COMMON, List.of(
                            "계약서/영수증 스캔하여 저장하기",
                            "한복 수령/반납 체크리스트 만들기",
                            "추가 비용 정산내역 정리하기",
                            "담당자 연락처 저장하기",
                            "한복집 이용 후기 작성하기"
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
                            "예복 스타일 레퍼런스 10장 이상 수집하기",
                            "맞춤 예복 제작 예산 설정하기",
                            "테일러샵 3곳 이상 후보 선정하기",
                            "원단 종류/등급 사전 조사하기",
                            "맞춤 제작 소요 기간 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "신랑 체형에 어울리는 예복 스타일 조사하기",
                            "드레스와 어울리는 예복 색상 정리하기",
                            "테일러샵 포트폴리오 비교하기",
                            "맞춤 제작 일정 체크하기",
                            "액세서리 조합 아이디어 정리하기"
                    ),
                    TodoType.COMMON, List.of(
                            "테일러샵 위치/교통편 확인하기",
                            "맞춤 제작 견적 비교표 만들기",
                            "실제 고객 후기 조사하기",
                            "상담 예약 일정 잡기",
                            "예약금 준비하기"
                    )
            ));

            // ON
            tailorShopStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "신체 상세 치수 측정하기",
                            "원단 샘플 직접 만져보고 선택하기",
                            "디자인 세부 사항 결정하기",
                            "1차 가봉 일정 잡기",
                            "제작 기간 및 가격 확정하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "신랑 체형에 맞는 실루엣 확인하기",
                            "원단과 드레스 색상 매치 확인하기",
                            "디자인 포인트 의견 전달하기",
                            "가봉 일정 캘린더에 기록하기",
                            "부자재 선택 의견 전달하기"
                    ),
                    TodoType.COMMON, List.of(
                            "계약서 필수 항목 꼼꼼히 체크하기",
                            "제작 일정표 상세 확인하기",
                            "예약금 결제하기",
                            "맞춤 제작 특이사항 기록하기",
                            "담당자 연락처 저장하기"
                    )
            ));

            // POST
            tailorShopStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "가봉 후 수정사항 상세 기록하기",
                            "셔츠/구두 맞춤 제작 여부 결정하기",
                            "최종 피팅 일정 확정하기",
                            "액세서리 구매 목록 작성하기",
                            "제작 완료 예복 보관 방법 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "가봉 피팅 사진 정리하기",
                            "최종 수정사항 전달하기",
                            "액세서리 코디 확정하기",
                            "본식 착용 팁 메모하기",
                            "픽업 일정 캘린더에 등록하기"
                    ),
                    TodoType.COMMON, List.of(
                            "계약서/영수증 스캔본 저장하기",
                            "잔금 납부 일정 체크하기",
                            "A/S 정책 확인하기",
                            "본식 전 피팅 일정 잡기",
                            "맞춤 제작 후기 작성하기"
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
                            "선호하는 스냅 포즈 5장 이상 저장하기",
                            "스냅 촬영 의상 준비 리스트 작성하기",
                            "야외 촬영 장소 후보 3곳 선정하기",
                            "헤어/메이크업 예약 확인하기",
                            "우천시 대체 장소 체크하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "스냅 촬영 컨셉 이미지 수집하기",
                            "드레스/한복 스냅 촬영 가능 여부 확인하기",
                            "헤어/메이크업 시간표 작성하기",
                            "소품/액세서리 준비 목록 만들기",
                            "날씨별 촬영 플랜 세우기"
                    ),
                    TodoType.COMMON, List.of(
                            "스냅 촬영 업체 3곳 이상 견적 받기",
                            "촬영 시간대/소요시간 확인하기",
                            "촬영 장소 동선 계획 세우기",
                            "비용 및 패키지 옵션 비교하기",
                            "예약금 준비하기"
                    )
            ));

            // ON
            snapStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "촬영 당일 복장 체크리스트 확인하기",
                            "원하는 포즈/표정 레퍼런스 전달하기",
                            "야외 촬영시 날씨 상황 체크하기",
                            "촬영 소품 준비물 점검하기",
                            "촬영 일정 타임테이블 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "헤어메이크업 상태 최종 점검하기",
                            "드레스/한복 상태 확인하기",
                            "액세서리 착용 상태 체크하기",
                            "비상용 메이크업 키트 준비하기",
                            "촬영 포즈 리스트 준비하기"
                    ),
                    TodoType.COMMON, List.of(
                            "촬영 장소별 동선 확인하기",
                            "비상연락망 저장하기",
                            "촬영 컨셉 최종 점검하기",
                            "결제 내역 확인하기",
                            "우천시 대체 장소 재확인하기"
                    )
            ));

            // POST
            snapStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "촬영본 수정 요청사항 정리하기",
                            "추가 촬영 컷 필요 여부 체크하기",
                            "사진 파일 백업 저장하기",
                            "결제 잔금 확인하기",
                            "보정 요청사항 전달하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "인생샷 보정 요청사항 정리하기",
                            "웨딩 앨범용 사진 선별하기",
                            "SNS 업로드용 사진 고르기",
                            "보정 강도 선호도 전달하기",
                            "추가 보정 필요 부분 체크하기"
                    ),
                    TodoType.COMMON, List.of(
                            "전체 촬영본 백업하기",
                            "사진 편집본 완성일 체크하기",
                            "앨범 구성 순서 정하기",
                            "촬영 후기 작성하기",
                            "추가 인화 필요 여부 결정하기"
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
                            "청첩장 예산 설정하기",
                            "신랑측 하객 명단 작성하기",
                            "청첩장 문구 의견 정리하기",
                            "배송비 포함 견적 확인하기",
                            "청첩장 제작 기간 체크하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "청첩장 디자인 스타일 정하기",
                            "신부측 하객 명단 작성하기",
                            "청첩장 샘플 3종 이상 비교하기",
                            "인사말 문구 후보 작성하기",
                            "청첩장 폰트/색상 선택하기"
                    ),
                    TodoType.COMMON, List.of(
                            "청첩장 제작업체 3곳 이상 비교하기",
                            "하객 주소록 엑셀로 정리하기",
                            "청첩장 수량 계산하기",
                            "모바일 청첩장 제작 여부 결정하기",
                            "샘플 청첩장 요청하기"
                    )
            ));

            // ON
            invitationStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "청첩장 문구 최종 검토하기",
                            "신랑측 하객 명단 최종 확정하기",
                            "청첩장 수량 재확인하기",
                            "배송 주소 정확성 체크하기",
                            "청첩장 제작 일정 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "청첩장 디자인 최종 확정하기",
                            "신부측 하객 명단 최종 확정하기",
                            "청첩장 샘플 최종 확인하기",
                            "인사말 문구 최종 선택하기",
                            "폰트/색상 최종 점검하기"
                    ),
                    TodoType.COMMON, List.of(
                            "청첩장 인쇄 교정 검토하기",
                            "배송 방법 결정하기",
                            "결제 진행하기",
                            "제작 일정 캘린더에 표시하기",
                            "모바일 청첩장 테스트하기"
                    )
            ));

            // POST
            invitationStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "신랑측 청첩장 발송 체크리스트 만들기",
                            "배송 상태 추적하기",
                            "누락된 하객 확인하기",
                            "모바일 청첩장 링크 테스트하기",
                            "청첩장 추가 주문 필요 여부 체크하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "신부측 청첩장 발송 체크리스트 만들기",
                            "청첩장 실물 보관용 준비하기",
                            "청첩장 발송 완료 여부 체크하기",
                            "모바일 청첩장 공유하기",
                            "하객 답변 현황 정리하기"
                    ),
                    TodoType.COMMON, List.of(
                            "청첩장 발송 완료 리스트 작성하기",
                            "영수증/계약서 스캔하여 보관하기",
                            "하객 리스트 최종 업데이트하기",
                            "청첩장 제작사 후기 작성하기",
                            "모바일 청첩장 최종 점검하기"
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
                            "피부 관리 예산 설정하기",
                            "피부과/관리샵 3곳 이상 알아보기",
                            "피부 관리 프로그램 비교하기",
                            "피부 관리 일정 체크하기",
                            "관리샵 위치/교통편 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "현재 피부 상태 체크리스트 작성하기",
                            "피부 타입별 관리법 조사하기",
                            "관리샵 후기/리뷰 비교하기",
                            "상담 예약 전 질문리스트 만들기",
                            "웨딩 전 피부 관리 계획 세우기"
                    ),
                    TodoType.COMMON, List.of(
                            "피부 관리샵 견적 비교표 만들기",
                            "관리 프로그램 옵션 조사하기",
                            "피부 관리 시작 시기 결정하기",
                            "예약금 준비하기",
                            "관리 주기 캘린더 작성하기"
                    )
            ));

            // ON
            facialCareStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "피부 타입 정밀 검사받기",
                            "관리 프로그램 일정 확정하기",
                            "피부 관리 전 주의사항 체크하기",
                            "알레르기 반응 테스트하기",
                            "관리 비용 결제 방법 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "피부 정밀 분석 검사받기",
                            "맞춤형 관리 프로그램 선택하기",
                            "트러블 부위 특별 관리 요청하기",
                            "홈케어 제품 리스트 받기",
                            "관리 전후 사진 촬영하기"
                    ),
                    TodoType.COMMON, List.of(
                            "피부 관리 계약서 검토하기",
                            "예약금 결제하기",
                            "정기 관리 일정 확정하기",
                            "특별 관리 항목 체크하기",
                            "응급 상황 대처법 확인하기"
                    )
            ));

            // POST
            facialCareStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "피부 변화 상태 기록하기",
                            "홈케어 제품 사용법 메모하기",
                            "다음 관리 일정 예약하기",
                            "피부 관리 비용 정산하기",
                            "주의사항 체크리스트 만들기"
                    ),
                    TodoType.FEMALE, List.of(
                            "피부 개선도 사진 기록하기",
                            "관리 전후 변화 기록하기",
                            "홈케어 루틴 일정표 만들기",
                            "추가 관리 필요 부위 체크하기",
                            "특별 관리 일정 조율하기"
                    ),
                    TodoType.COMMON, List.of(
                            "관리 결과 만족도 평가하기",
                            "영수증/계약서 보관하기",
                            "다음 예약 일정 캘린더에 기록하기",
                            "관리샵 이용 후기 작성하기",
                            "결제 내역 정리하기"
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
                            "신혼여행 예산 범위 설정하기",
                            "희망 여행지 3곳 이상 조사하기",
                            "여행사 견적 비교표 만들기",
                            "여권 유효기간 확인하기",
                            "여행 보험 상품 알아보기"
                    ),
                    TodoType.FEMALE, List.of(
                            "신혼여행지 컨셉 정하기",
                            "호텔/리조트 후보 리스트 작성하기",
                            "현지 관광지 정보 수집하기",
                            "여행 일정 초안 작성하기",
                            "필요한 리조트 웨어 리스트 만들기"
                    ),
                    TodoType.COMMON, List.of(
                            "여행사 3곳 이상 상담 예약하기",
                            "여행 가능 기간 체크하기",
                            "여권/비자 발급 준비하기",
                            "예방 접종 필요 여부 확인하기",
                            "신혼여행 패키지 옵션 비교하기"
                    )
            ));

            // ON
            honeymoonStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "여행지 날씨/기후 정보 확인하기",
                            "환전 계획 세우기",
                            "여행자보험 가입하기",
                            "항공권 마일리지 적립 확인하기",
                            "여행 경비 카드 한도 조정하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "호텔 룸타입/전망 선택하기",
                            "데이투어 일정 확정하기",
                            "현지 레스토랑 예약 확인하기",
                            "여행지별 준비물 리스트 작성하기",
                            "스파/마사지 예약 확인하기"
                    ),
                    TodoType.COMMON, List.of(
                            "여권/비자 발급 완료하기",
                            "필수 예방접종 완료하기",
                            "여행 일정표 최종 확정하기",
                            "계약금 납부하기",
                            "여행 필수 서류 체크리스트 만들기"
                    )
            ));

            // POST
            honeymoonStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "여행 서류 스캔본 저장하기",
                            "여행자보험 보장 내용 확인하기",
                            "현지 필수 연락처 저장하기",
                            "환전 수령 일정 체크하기",
                            "여행 경비 정산 계획 세우기"
                    ),
                    TodoType.FEMALE, List.of(
                            "여행지별 복장 코디 정리하기",
                            "액티비티별 준비물 체크하기",
                            "호텔 특별 요청사항 전달하기",
                            "여행 일정표 프린트하기",
                            "여행용 세면도구 준비리스트 작성하기"
                    ),
                    TodoType.COMMON, List.of(
                            "여행 필수품 쇼핑리스트 작성하기",
                            "캐리어 무게 제한 확인하기",
                            "여행 일정 최종 리뷰하기",
                            "여행사 긴급연락망 저장하기",
                            "출국 전 체크리스트 작성하기"
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
                            "결혼식 전날 숙소 예약하기",
                            "당일 이동 차량 확인하기",
                            "비상용 정장 셔츠 준비하기",
                            "혼주 의상/준비물 체크하기",
                            "당일 스케줄 시간표 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "웨딩드레스 최종 피팅하기",
                            "헤어/메이크업 리허설 완료하기",
                            "웨딩슈즈 착화감 테스트하기",
                            "비상용 메이크업 키트 준비하기",
                            "웨딩 소품 체크리스트 작성하기"
                    ),
                    TodoType.COMMON, List.of(
                            "식순 타임테이블 최종 점검하기",
                            "하객 안내 도우미 배치 확인하기",
                            "축의금 보관 담당자 지정하기",
                            "비상연락망 전달하기",
                            "결혼식 리허설 진행하기"
                    )
            ));

            // ON
            weddingDayStages.put(StageType.ON, Map.of(
                    TodoType.MALE, List.of(
                            "신랑 대기실 정리/세팅 확인하기",
                            "혼주 영접/안내하기",
                            "축의금 보관 담당자와 연락하기",
                            "사회자/주례와 최종 동선 점검하기",
                            "웨딩카 준비 상태 체크하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "웨딩드레스 최종 상태 점검하기",
                            "헤어메이크업 완성도 체크하기",
                            "부케/장갑/티아라 착용 확인하기",
                            "웨딩슈즈 상태 점검하기",
                            "비상용 메이크업 키트 확인하기"
                    ),
                    TodoType.COMMON, List.of(
                            "하객 방명록/축의금 접수 확인하기",
                            "식사/연출 진행 상황 체크하기",
                            "웨딩홀 장식/조명 점검하기",
                            "포토테이블 세팅 확인하기",
                            "비상연락망 최종 점검하기"
                    )
            ));

            // POST
            weddingDayStages.put(StageType.POST, Map.of(
                    TodoType.MALE, List.of(
                            "축의금/방명록 정리 확인하기",
                            "대기실 귀중품 수거하기",
                            "웨딩카 반납 확인하기",
                            "양가 부모님 귀가 동행하기",
                            "정산 내역 확인하기"
                    ),
                    TodoType.FEMALE, List.of(
                            "드레스/소품 반납 체크하기",
                            "메이크업/헤어 클렌징 용품 준비하기",
                            "웨딩 소품 수거 확인하기",
                            "신부대기실 정리하기",
                            "기념 사진 백업하기"
                    ),
                    TodoType.COMMON, List.of(
                            "하객 답례품 전달 확인하기",
                            "웨딩홀 잔금 정산하기",
                            "축하 화환 처리 방법 확인하기",
                            "양가 혼주 감사 인사드리기",
                            "웨딩 업체 스태프 정산하기"
                    )
            ));

            data.put(ServiceType.WEDDING_DAY, weddingDayStages);
        }

        return data;
    }
}