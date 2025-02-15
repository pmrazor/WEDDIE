package com.ssafy.exhi.domain;

import com.ssafy.exhi.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/time")
public class TimeController {
    private final TimeWrapper timeWrapper; // 객체 주입

    // 현재 시간 설정하는 get API
    @GetMapping
    public void setDemoTime(@RequestBody LocalDateTime demoTime) {
        timeWrapper.setCurrentTime(demoTime);
    }

    // 현재 시간에 일수를 추가하는 API
    @PostMapping
    public ResponseEntity<?> addDays(@RequestParam(name="days") Integer days) {
        return ResponseEntity.ok().body(ApiResponse.onSuccess(timeWrapper.addDays(days)));
    }
}