package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import com.ssafy.exhi.domain.todo.repository.TodoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final DefaultTodoRepository defaultTodoRepository;
    private final PlanRepository planRepository;

    @Override
    public void createTodo(Integer UserId) {
        // 서비스 타입 별로 담당 기본 투두 생성해주기

        // 1. 기본 투두는 어디에서? 디폴트 투두에서
        List<DefaultTodo> list = defaultTodoRepository.findAll();

        for (DefaultTodo todo : list) {

        }
    }
}
