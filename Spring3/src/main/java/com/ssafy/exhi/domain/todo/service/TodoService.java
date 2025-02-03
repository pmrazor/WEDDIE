package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import jakarta.validation.Valid;

public interface TodoService {
    TodoResponse.DetailResultDTO createTodo(Integer UserId, TodoRequest.CreateDTO createDTO);

    TodoResponse.DetailResultDTO patchTodo(Integer userId, TodoRequest.UpdateDTO updateDTO);

    TodoResponse.DetailResultDTO updateTodo(Integer userId, TodoRequest.CheckDTO checkDTO);

    void deleteTodo(Integer userId, Integer planId, Integer todoId);
}
