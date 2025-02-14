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
    private final TimeWrapper timeWrapper;

    @GetMapping
    public void setDemoTime(@RequestBody LocalDateTime demoTime) {
        timeWrapper.setCurrentTime(demoTime);
    }

    @PostMapping
    public void addDays(@RequestParam long days) {
        timeWrapper.addDays(days);
    }


}
