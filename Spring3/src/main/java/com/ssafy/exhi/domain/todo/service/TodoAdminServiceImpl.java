package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.domain.todo.converter.TodoConverter;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoResponse;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class TodoAdminServiceImpl implements TodoAdminService {

    private final DefaultTodoRepository defaultTodoRepository;

    @Override
    public DefaultTodoResponse.DetailResultDTO createDefaultTodo(DefaultTodoRequest.CreateDTO dto) {
        DefaultTodo defaultTodo = TodoConverter.toDefaultTodo(dto);
        DefaultTodo saved = defaultTodoRepository.save(defaultTodo);

        return TodoConverter.toDefaultDTO(saved);
    }
}
