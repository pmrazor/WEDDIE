package com.ssafy.exhi.domain.todo.controller;

import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.DefaultTodoResponse;
import com.ssafy.exhi.domain.todo.service.TodoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/todo")
public class TodoAdminController {

    private final TodoAdminService todoAdminService;

    @GetMapping
    public ResponseEntity<?> createDefaultTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @RequestBody @Valid DefaultTodoRequest.CreateDTO createDTO
    ) {
        // TODO 관리자 인증 절차 추가

        DefaultTodoResponse.DetailResultDTO response = todoAdminService.createDefaultTodo(createDTO);
        return ResponseEntity.ok(response);
    }
}
