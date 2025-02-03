package com.ssafy.exhi.domain.todo.converter;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import com.ssafy.exhi.domain.todo.model.entity.Todo;

import java.util.List;
import java.util.stream.Collectors;

public class TodoConverter {
    public static TodoResponse.DetailResultDTO toDTO(Todo todo) {
        return TodoResponse.DetailResultDTO.builder()
                .todoId(todo.getId())
                .content(todo.getContent())
                .serviceType(todo.getServiceType())
                .isCompleted(todo.isCompleted())
                .build();
    }

    public static List<TodoResponse.DetailResultDTO> toDTO(List<Todo> todos) {
        return todos.stream()
                .map(TodoConverter::toDTO)
                .collect(Collectors.toList());
    }

    public static Todo toEntity(Plan plan, TodoRequest.CreateDTO dto) {
        return Todo.builder()
                .plan(plan)
                .content(dto.getContent())
                .serviceType(plan.getServiceType())
                .isCompleted(false)
                .build();
    }
}
