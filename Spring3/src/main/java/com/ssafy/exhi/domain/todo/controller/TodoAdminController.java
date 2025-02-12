package com.ssafy.exhi.domain.todo.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoResponse;
import com.ssafy.exhi.domain.todo.repository.DefaultTodoRepository;
import com.ssafy.exhi.domain.todo.service.TodoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/todo")
public class TodoAdminController {

    private final TodoAdminService todoAdminService;
    private final DefaultTodoRepository defaultTodoRepository;

    @GetMapping
    public ResponseEntity<?> getDefaultTodos() {
        List<DefaultTodoResponse.DetailResultDTO> response = todoAdminService.getDefaultTodos();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostMapping
    public ResponseEntity<?> createDefaultTodo(
        @RequestHeader(name = "Authorization") String accessToken,
        @RequestBody @Valid DefaultTodoRequest.CreateDTO createDTO
    ) {
        // TODO 관리자 인증 절차 추가

        DefaultTodoResponse.DetailResultDTO response = todoAdminService.createDefaultTodo(createDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
