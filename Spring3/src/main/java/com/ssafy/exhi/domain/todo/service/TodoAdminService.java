package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoResponse;

import java.util.List;

public interface TodoAdminService {
    DefaultTodoResponse.DetailResultDTO createDefaultTodo(DefaultTodoRequest.CreateDTO dto);

    List<DefaultTodoResponse.DetailResultDTO> getDefaultTodos();
}
