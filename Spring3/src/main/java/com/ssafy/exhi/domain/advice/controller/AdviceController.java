package com.ssafy.exhi.domain.advice.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.advice.service.AdviceService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan/advice")
public class AdviceController {

    private final AdviceService adviceService;
    private final JWTUtil jwtUtil;

    @PostMapping("/{messageType}")
    public ResponseEntity<?> getAdvice(@RequestHeader("Authorization") String accessToken, @PathVariable("messageType") boolean messageType) {
        Integer userId = jwtUtil.getUserId(accessToken);
        String advice = adviceService.getAdvice(userId, messageType);
        return ResponseEntity.ok(ApiResponse.onSuccess(advice));
    }
}
