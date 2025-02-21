package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
import com.ssafy.exhi.domain.tip.repository.TipRepository;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import com.ssafy.exhi.domain.todo.repository.TodoRepository;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.domain.virginroad.repository.VirginRoadRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TodosServiceImpl implements TodosService {
    // 의존성 주입 (레포지토리 등)
    private final UserDetailRepository userDetailRepository;
    private final VirginRoadRepository virginroadRepository;
    private final TodoRepository todoRepository;
    private final CoupleRepository coupleRepository;
    private final TipRepository tipRepository;

    /**
     * 투두 생성 로직
     */
    @Override
    public TodoResponse.DetailResultDTO createTodo(Integer userId, TodoRequest.CreateTodosDTO createDTO) {
        Integer coupleId = coupleRepository.findCoupleIdByUserId(userId);
        if (coupleId == null) {
            throw new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND);
        }

        // 유저가 속한 버진로드에서 해당 서비스 타입의 Plan 가져오기
        Plan plan = getPlanByServiceType(userId, coupleId, createDTO.getServiceType());

        // 새로운 투두 엔티티 생성 후 저장
        Todo newTodo = Todo.builder()
                .plan(plan)
                .serviceType(createDTO.getServiceType())
                .stage(createDTO.getStageType())
                .content(createDTO.getContent())
                .todoType(createDTO.getTodoType())
                .isCompleted(false)
                .build();
        todoRepository.save(newTodo);

        return TodoResponse.DetailResultDTO.builder()
                .todoId(newTodo.getId())
                .serviceType(newTodo.getServiceType())
                .content(newTodo.getContent())
                .isCompleted(newTodo.isCompleted())
                .build();
    }

    /**
     * 특정 서비스 타입의 투두 리스트 조회
     * - 사용자 성별(Gender)에 따라 MALE 혹은 FEMALE 투두 + COMMON 투두만 보여준다.
     */
    @Override
    public TodoResponse.FindAllResultDTO readTodos(Integer userId, ServiceType serviceType) {
        Integer coupleId = coupleRepository.findCoupleIdByUserId(userId);

        if (coupleId == null) {
            throw new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND);
        }

        // 특정 유저가 속한 버진로드에서 해당 서비스 타입의 Plan 조회
        List<Todo> todos = getTodos(userId, coupleId, serviceType);

        List<TodoResponse.FindResultDTO> preTodos = new ArrayList<>();
        List<TodoResponse.FindResultDTO> onTodos = new ArrayList<>();
        List<TodoResponse.FindResultDTO> postTodos = new ArrayList<>();

        // 투두 stage별로 구분
        for (Todo todo : todos) {
            TodoResponse.FindResultDTO dto = TodoResponse.FindResultDTO.builder()
                    .todoId(todo.getId())
                    .content(todo.getContent())
                    .isCompleted(todo.isCompleted())
                    .build();
            switch (todo.getStage()) {
                case PRE -> preTodos.add(dto);
                case ON -> onTodos.add(dto);
                case POST -> postTodos.add(dto);
            }
        }

        Tip tip = tipRepository.findTipsByServiceType(serviceType)
            .stream()
            .findFirst()
            .orElse(null);  // Tip이 없으면 null로 설정

        return TodoResponse.FindAllResultDTO.builder()
                .planId(todos.get(0).getPlan().getId())
                .serviceType(serviceType)
                .todos(Map.of(
                        "pre", preTodos,
                        "on", onTodos,
                        "post", postTodos
                ))
                .tip(tip == null ? "" : tip.getTipContent())
                .build();
    }

    /**
     * 투두 isCompleted 상태 업데이트
     */
    @Override
    public TodoResponse.FindResultDTO updateTodo(Integer todoId, Boolean isCompleted) {
        Todo todo = getTodoByTodoId(todoId);
        todo.setCompleted(isCompleted);
        todoRepository.save(todo);

        return TodoResponse.FindResultDTO.builder()
                .todoId(todo.getId())
                .content(todo.getContent())
                .isCompleted(isCompleted)
                .build();
    }

    /**
     * 투두 삭제
     */
    @Override
    public void deleteTodo(Integer todoId) {
        Todo todo = getTodoByTodoId(todoId);
        todoRepository.delete(todo);
    }

    /**
     * 헬퍼 메서드
     * 버진로드 가져오기
     */
    private VirginRoad getVirginroad(Integer coupleId) {
        return virginroadRepository.findVirginRoadByCoupleId(coupleId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_NOT_FOUND));
    }

    /**
     * 특정 todoId로 Todo 엔티티 찾기
     */
    private Todo getTodoByTodoId(Integer todoId) {
        return todoRepository.findTodoById(todoId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.TODO_NOT_FOUND));
    }

    /**
     * 특정 유저의 성별과 Plan 정보를 바탕으로 Todo 목록 조회
     * - userId -> gender 구해서, MALE/FEMALE/COMMON 만 가져온다.
     */
    private List<Todo> getTodos(Integer userId, Integer coupleId, ServiceType serviceType) {
        // 버진로드 찾기
        VirginRoad virginRoad = virginroadRepository.findVirginRoadByCoupleId(coupleId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.VIRGIN_ROAD_NOT_FOUND));

        // 해당 버진로드에서 serviceType에 맞는 Plan 찾기
        Plan plan = virginRoad.findPlanByServiceType(serviceType);
        if (plan == null) {
            throw new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND);
        }

        // 사용자 성별 탐색
        Gender userGender = getUserGender(userId);
        List<TodoType> todoTypes = new ArrayList<>();
        // 공통 Todo
        todoTypes.add(TodoType.COMMON);
        // 남자이면 MALE, 여자이면 FEMALE
        if (userGender == Gender.MALE) {
            todoTypes.add(TodoType.MALE);
        } else {
            todoTypes.add(TodoType.FEMALE);
        }

        return todoRepository.findByPlanIdAndTodoTypeIn(plan.getId(), todoTypes);
    }

    /**
     * userId로부터 UserDetail 찾고, gender 반환
     */
    private Gender getUserGender(Integer userId) {
        return userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND))
                .getGender();
    }

    /**
     * 특정 user/couple/serviceType에 맞는 Plan 찾기
     */
    private Plan getPlanByServiceType(Integer userId, Integer coupleId, ServiceType serviceType) {
        VirginRoad virginRoad = getVirginroad(coupleId);
        Plan plan = virginRoad.findPlanByServiceType(serviceType);
        if (plan == null) {
            throw new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND);
        }
        return plan;
    }
}
