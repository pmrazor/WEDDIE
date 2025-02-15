package com.ssafy.exhi.domain.todo.service;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;

public interface TodosService {
    TodoResponse.DetailResultDTO createTodo(Integer userId, Integer coupleId, TodoRequest.CreateTodosDTO createDTO);
    TodoResponse.FindAllResultDTO readTodos(Integer userId, Integer coupleId, ServiceType serviceType);
    TodoResponse.FindResultDTO updateTodo(Integer todoId, Boolean isCompleted);
    void deleteTodo(Integer todoId);

}
