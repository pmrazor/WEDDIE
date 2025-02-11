package com.ssafy.exhi.domain.advice.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.ServiceType;
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

    @PostMapping("/{serviceType}")
    public ResponseEntity<?> getAdvice(@RequestHeader("Authorization") String accessToken, @PathVariable("serviceType") ServiceType serviceType) {
        Integer userId = jwtUtil.getUserId(accessToken);
        String advice = adviceService.getAdvice(userId, serviceType);
        return ResponseEntity.ok(ApiResponse.onSuccess(advice));
    }
}
