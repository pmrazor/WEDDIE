package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;

import java.util.List;

public interface TodoService {
    TodoResponse.DetailResultDTO createTodo(Integer UserId, TodoRequest.CreateDTO createDTO);

    TodoResponse.DetailResultDTO patchTodo(Integer userId, TodoRequest.UpdateDTO updateDTO);

    TodoResponse.DetailResultDTO updateTodo(Integer userId, TodoRequest.CheckDTO checkDTO);

    List<TodoResponse.DetailResultDTO> getTodo(Integer userId, Integer planId);

    void deleteTodo(Integer userId, Integer planId, Integer todoId);
}
