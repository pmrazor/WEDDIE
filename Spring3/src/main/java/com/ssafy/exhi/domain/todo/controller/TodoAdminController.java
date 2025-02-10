package com.ssafy.exhi.domain.todo.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoResponse;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import com.ssafy.exhi.domain.todo.service.TodoAdminService;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/todo")
public class TodoAdminController {

    private final TodoAdminService todoAdminService;
    private final DefaultTodoRepository defaultTodoRepository;

    @GetMapping
    public ResponseEntity<?> getDefaultTodos() {
        List<DefaultTodoResponse.DetailResultDTO> response = todoAdminService.getDefaultTodos();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostMapping
    public ResponseEntity<?> createDefaultTodo(
        @RequestHeader(name = "Authorization") String accessToken,
        @RequestBody @Valid DefaultTodoRequest.CreateDTO createDTO
    ) {
        // TODO 관리자 인증 절차 추가

        DefaultTodoResponse.DetailResultDTO response = todoAdminService.createDefaultTodo(createDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostConstruct
    public void initData() {

        // 본식 당일 (신부) Todo 데이터 생성
        List<String> brideWeddingDayTodo = Arrays.asList(
            "DVD, 스냅 작가 도착 시간 체크",
            "메이크업 샵에서 부케, 부토니에 챙기기",
            "메이크업 샵에서 큰 거울 앞에서 드레스 입을 때 이상한 점 없는지 체크",
            "드레스 오염 상태 체크",
            "제모, 네일케어, 속옷 확인",
            "당일 가방순이에게 가방 전달",
            "본식 끝난 후, 혼주 한복 반납 체크",
            "웨딩 액자 다시 챙기기",
            "신부 측 최종 비용 결제"
        );
        for (String content : brideWeddingDayTodo) {
            defaultTodoRepository.save(DefaultTodo.builder()
                .serviceType(ServiceType.WEDDING_DAY)
                .content(content)
                .gender(Gender.FEMALE)
                .build());
        }

        // 본식 당일 (신랑) Todo 데이터 생성
        List<String> groomWeddingDayTodo = Arrays.asList(
            "당일 면도",
            "웨딩홀에 차량 주차 후 위치 알려주기",
            "포토테이블에 사진 및 웨딩 액자 전달",
            "축의금 접수자에게 안내사항 전달",
            "사회자와 진행 순서, 내용 대본 상의",
            "부토니에 착용",
            "사회자, 주례, 혼주 4명 코사지 확인",
            "예식 1시간 전부터 인사",
            "3~40분간 신부 대기실에서 컨셉사진 촬영",
            "식 진행 이벤트 체크 (예식 순서, 성혼 선언문, 사회, 입장, 주례, 축가 등)",
            "신랑 측 최종비용 결제",
            "하객 및 혼주 주차 조건/ 시간/ 비용 확인"
        );
        for (String content : groomWeddingDayTodo) {
            defaultTodoRepository.save(DefaultTodo.builder()
                .serviceType(ServiceType.WEDDING_DAY)
                .content(content)
                .gender(Gender.MALE)
                .build());
        }

        // 스튜디오 (신부) Todo 데이터 생성
        List<String> brideStudioTodo = Arrays.asList(
            "웨딩 네일",
            "촬영부케",
            "액세서리",
            "촬영용 드레스",
            "촬영용 신발"
        );
        for (String content : brideStudioTodo) {
            defaultTodoRepository.save(DefaultTodo.builder()
                .serviceType(ServiceType.STUDIO)
                .content(content)
                .gender(Gender.FEMALE)
                .build());
        }

        // 스튜디오 (신랑) Todo 데이터 생성
        List<String> groomStudioTodo = Arrays.asList(
            "헬퍼비 (현금)",
            "간식",
            "액세서리",
            "정장 세트",
            "다양한 색상 넥타이와 벨트",
            "촬영용 신발들"
        );
        for (String content : groomStudioTodo) {
            defaultTodoRepository.save(DefaultTodo.builder()
                .serviceType(ServiceType.STUDIO)
                .content(content)
                .gender(Gender.MALE)
                .build());
        }

        // 드레스샵 (신부) Todo 데이터 생성
        List<String> brideDressShopTodo = Arrays.asList(
            "웨딩 브라",
            "얇은 스킨색 속바지",
            "얇은 스킨색 노라인 햄팬티",
            "스타킹 신지 말기 (신는다면 살색)",
            "탈의에 지장을 주지 않도록 단추로 열고 닫을 수 있는 옷",
            "신고 벗기 편한 신발",
            "제모"
        );
        for (String content : brideDressShopTodo) {
            defaultTodoRepository.save(DefaultTodo.builder()
                .serviceType(ServiceType.DRESS_SHOP)
                .content(content)
                .gender(Gender.FEMALE)
                .build());
        }

        // 드레스샵 (신랑) Todo 데이터 생성
        List<String> groomDressShopTodo = Arrays.asList(
            "피팅비 카드 가능 여부 체크 및 현금 지참",
            "주차비 혹은 발레파킹 현금 지참"
        );
        for (String content : groomDressShopTodo) {
            defaultTodoRepository.save(DefaultTodo.builder()
                .serviceType(ServiceType.DRESS_SHOP)
                .content(content)
                .gender(Gender.MALE)
                .build());
        }
    }
}
