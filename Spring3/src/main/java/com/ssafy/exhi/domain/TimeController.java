
package com.ssafy.exhi.domain;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public void addDays(@RequestParam long days) {
        timeWrapper.addDays(days);
    }


}