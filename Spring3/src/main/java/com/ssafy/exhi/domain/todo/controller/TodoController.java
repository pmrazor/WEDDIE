package com.ssafy.exhi.domain.todo.controller;

import com.ssafy.exhi.domain.todo.service.TodoService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo")
public class TodoController {

    private final JWTUtil jwtUtil;
    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<?> getTodos(
            @RequestHeader(name = "Authorization") String accessToken
    ) {


        return ResponseEntity.ok(null);
    }

    /**
     *
     * @param accessToken 유저 토큰
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createTodo(
            @RequestHeader(name = "Authorization") String accessToken
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        todoService.createTodo(userId);
        return ResponseEntity.ok(null);
    }

}
