package com.ssafy.exhi.domain.todo.converter;

import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoResponse;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import com.ssafy.exhi.domain.todo.model.entity.DefaultTodo;
import com.ssafy.exhi.domain.todo.model.entity.Todo;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;

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

    public static Todo toEntity(Plan plan, TodoRequest.CreateDTO dto, TodoType todoType) {
        return Todo.builder()
            .plan(plan)
            .content(dto.getContent())
            .serviceType(plan.getServiceType())
            .todoType(todoType)
            .isCompleted(false)
            .build();
    }

    public static DefaultTodo toDefaultTodo(DefaultTodoRequest.CreateDTO dto) {
        return DefaultTodo.builder()
            .content(dto.getContent())
            .serviceType(dto.getServiceType())
            .gender(dto.getGender())
            .build();
    }

    public static DefaultTodoResponse.DetailResultDTO toDefaultDTO(DefaultTodo entity) {
        return DefaultTodoResponse.DetailResultDTO.builder()
            .defaultTodoId(entity.getId())
            .gender(entity.getGender())
            .content(entity.getContent())
            .serviceType(entity.getServiceType())
            .build();
    }

    public static List<DefaultTodoResponse.DetailResultDTO> toDefaultDTOList(List<DefaultTodo> defaultTodos) {
        return defaultTodos.stream()
            .map(TodoConverter::toDefaultDTO)
            .toList();
    }
}
