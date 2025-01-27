package com.ssafy.exhi.domain.plan.controller;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.plan.model.dto.PlanRequest;
import com.ssafy.exhi.domain.plan.model.dto.PlanResponse;
import com.ssafy.exhi.domain.plan.service.PlanService;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.util.JWTUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ssafy.exhi.domain.plan.model.dto.PlanResponse.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/plan")
public class PlanController {

    private final JWTUtil jwtUtil;
    private final PlanService planService;

    /**
     * 온보딩에 사용되는 API
     * 결혼 과정에 필요한 모든 row를 생성한다.
     * @param createListDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createPlan(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PlanRequest.CreateListDTO createListDTO
    ) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new ExceptionHandler(ErrorStatus.TOKEN_NOT_FOUND);
        }
        if (createListDTO == null) {
            throw new ExceptionHandler(ErrorStatus.NOT_ENOUGH_REQUEST_BODY_ERROR);
        }

        Integer userId = jwtUtil.getUserId(accessToken);
        createListDTO.setUserId(userId);
        log.info("createPlan userId: {}", userId);

        List<SimpleResultDTO> response = planService.createPlan(createListDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getPlan() {

        return ResponseEntity.ok(null);
    }
}
