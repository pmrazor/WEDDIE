package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.todo.converter.TodoConverter;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.todo.repository.TodoRepository;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final CoupleRepository coupleRepository;
    private final TodoRepository todoRepository;
    private final PlanRepository planRepository;

    @Override
    public TodoResponse.DetailResultDTO createTodo(Integer userId, TodoRequest.CreateDTO createDTO) {
        Couple couple = getCoupleByUserId(userId);
        Plan plan = getPlanById(createDTO.getPlanId());
        checkPlanOwnerShip(couple, plan);

        Todo todo = TodoConverter.toEntity(plan, createDTO);
        Todo saved = todoRepository.save(todo);

        return TodoConverter.toDTO(saved);
    }

    @Override
    public TodoResponse.DetailResultDTO patchTodo(Integer userId, TodoRequest.UpdateDTO updateDTO) {
        Couple couple = getCoupleByUserId(userId);
        Plan plan = getPlanById(updateDTO.getPlanId());
        checkPlanOwnerShip(couple, plan);

        Todo todo = getTodoById(updateDTO.getTodoId());
        todo.patch(updateDTO);

        return TodoConverter.toDTO(todo);
    }

    @Override
    public TodoResponse.DetailResultDTO updateTodo(Integer userId, TodoRequest.CheckDTO checkDTO) {
        Couple couple = getCoupleByUserId(userId);
        Plan plan = getPlanById(checkDTO.getPlanId());
        checkPlanOwnerShip(couple, plan);

        Todo todo = getTodoById(checkDTO.getTodoId());
        todo.check(checkDTO);

        return TodoConverter.toDTO(todo);
    }

    @Override
    public void deleteTodo(Integer userId, Integer planId, Integer todoId) {
        Couple couple = getCoupleByUserId(userId);
        Plan plan = getPlanById(planId);
        checkPlanOwnerShip(couple, plan);

        Todo todo = getTodoById(todoId);
        todoRepository.delete(todo);
    }

    private void checkPlanOwnerShip(Couple couple , Plan plan) {
        Integer coupleId = Optional.ofNullable(plan)
                .map(Plan::getVirginRoad)
                .map(VirginRoad::getCouple)
                .map(Couple::getId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus._FORBIDDEN));

        if (!Objects.equals(couple.getId(), coupleId)) {
            throw new ExceptionHandler(ErrorStatus._FORBIDDEN);
        }
    }

    private Todo getTodoById(Integer todoId) {
        return todoRepository.findTodoById(todoId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.TODO_NOT_FOUND)
        );
    }

    private Couple getCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND)
        );
    }

    private Plan getPlanById(Integer planId) {
        return planRepository.findPlanById(planId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND)
        );
    }
}
