package com.ssafy.exhi.domain.todo.controller;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.todo.model.dto.TodoRequest;
import com.ssafy.exhi.domain.todo.model.dto.TodoResponse;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
import com.ssafy.exhi.domain.todo.service.TodoService;
import com.ssafy.exhi.domain.todo.service.TodosService;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.util.JWTUtil;
import com.ssafy.exhi.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo")
public class TodosController {

    private final JWTUtil jwtUtil;
    private final TodosService todosService;
    private final TodoService todoService;
    private final PlanRepository planRepository;
    private final UserDetailRepository userDetailRepository;

    @PostMapping("/createTodo")
    public ResponseEntity<?> createTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @RequestBody TodoRequest.CreateTodosDTO createDTO
    ) {
        try {
            if (createDTO.getContent() == null || createDTO.getStageType() == null || createDTO.getPlanId() == null ) {
                return ResponseEntity.badRequest().body(ErrorStatus.NOT_ENOUGH_REQUEST_BODY_ERROR.getReason());
            }

            Integer userId = jwtUtil.getUserId(accessToken);
//            Integer coupleId = jwtUtil.getCoupleId(accessToken);

            ServiceType serviceType = planRepository.findPlanById(createDTO.getPlanId()).orElseThrow(() -> new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND)).getServiceType();
            createDTO.setServiceType(serviceType);

            String gender = userDetailRepository.findByUserId(userId).orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)).getGender().toString();
            TodoType todoType = createDTO.isTogether() ? TodoType.COMMON : TodoType.valueOf(gender);
            createDTO.setTodoType(todoType);

            TodoResponse.DetailResultDTO response = todosService.createTodo(userId, createDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorStatus._BAD_REQUEST.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorStatus._INTERNAL_SERVER_ERROR.getReason());
        }
    }

    @GetMapping("/getTodos/{serviceType}")
    public ResponseEntity<?> readTodos(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("serviceType") String serviceTypeStr
    ) {
        try {
            ServiceType serviceType = ServiceType.valueOf(serviceTypeStr.toUpperCase());

            Integer userId = jwtUtil.getUserId(accessToken);

            TodoResponse.FindAllResultDTO response = todosService.readTodos(userId, serviceType);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid serviceType provided");
        }
    }

    @PatchMapping("/{todoId}")
    public ResponseEntity<?> updateTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("todoId") Integer todoId,
            @RequestBody Map<String, Object> requestBody
    ) {
        try {
            if (!requestBody.containsKey("isCompleted")) {
                return ResponseEntity.badRequest().body("Missing 'isCompleted' field");
            }

            Boolean isCompleted = (Boolean) requestBody.get("isCompleted");
            TodoResponse.FindResultDTO response = todosService.updateTodo(todoId, isCompleted);
            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            return ResponseEntity.badRequest().body("Invalid 'isCompleted' value");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<?> deleteTodo(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("todoId") Integer todoId
    ) {
        todosService.deleteTodo(todoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("{planId}/{todoId}")
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
}
