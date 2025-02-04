package com.ssafy.exhi.domain.timecapsule.Controller;

import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleRequest;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleResponse;
import com.ssafy.exhi.domain.timecapsule.service.TimeCapsuleService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timeCapsule")
public class TimeCapsuleController {

    private final TimeCapsuleService timeCapsuleService;
    private final JWTUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createTimeCapsule(@Valid @RequestHeader("Authorization") String accessToken, @RequestBody TimeCapsuleRequest.CreateDTO timeCapsuleDto) {
        Integer userId = jwtUtil.getUserId(accessToken);
        timeCapsuleDto.setUserId(userId);
        TimeCapsuleResponse.TimeCapsuleResponseDto response = timeCapsuleService.createTimeCapsule(timeCapsuleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getTimeCapsulesByCoupleId(@RequestHeader("Authorization") String accessToken) {

        Integer userId = jwtUtil.getUserId(accessToken);

        List<TimeCapsuleResponse.TimeCapsuleResponseDto> response = timeCapsuleService.findTimeCapsulesByCoupleId(userId);
        return ResponseEntity.ok(response);
    }
}
