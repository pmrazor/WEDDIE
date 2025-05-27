package com.ssafy.exhi.domain.todo.controller;

import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import com.ssafy.exhi.domain.todo.service.TodoService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class TodoController {

    private final JWTUtil jwtUtil;
    private final TodoService todoService;

    @GetMapping("/{planId}/todo")
    public ResponseEntity<?> getTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("planId") Integer planId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        List<TodoResponse.DetailResultDTO> response = todoService.getTodo(userId, planId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     *
     * @param accessToken 유저 토큰
     * @return
     */
    @PostMapping("/{planId}/todo")
    public ResponseEntity<?> createTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("planId") Integer planId,
            @RequestBody @Valid TodoRequest.CreateDTO createDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        createDTO.setPlanId(planId);

        TodoResponse.DetailResultDTO response = todoService.createTodo(userId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     *  투두 컨텐츠를 수정하는 API
     */
    @PatchMapping("/{planId}/todo/{todoId}")
    public ResponseEntity<?> patchTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("planId") Integer planId,
            @PathVariable("todoId") Integer todoId,
            @RequestBody @Valid TodoRequest.UpdateDTO updateDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        updateDTO.setPlanId(planId);
        updateDTO.setTodoId(todoId);

        TodoResponse.DetailResultDTO response = todoService.patchTodo(userId, updateDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * 투두 체크를 수정하는 API
     */
    @PutMapping("/{planId}/todo/{todoId}")
    public ResponseEntity<?> updateTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("planId") Integer planId,
            @PathVariable("todoId") Integer todoId,
            @RequestBody @Valid TodoRequest.CheckDTO checkDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        checkDTO.setPlanId(planId);
        checkDTO.setTodoId(todoId);

        TodoResponse.DetailResultDTO response = todoService.updateTodo(userId, checkDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * 투두 삭제 API
     */
    @DeleteMapping("/{planId}/todo/{todoId}")
    public ResponseEntity<?> deleteTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("planId") Integer planId,
            @PathVariable("todoId") Integer todoId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        todoService.deleteTodo(userId, planId, todoId);

        return ResponseEntity.noContent().build();
    }

}
