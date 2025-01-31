package com.ssafy.exhi.domain.notification;

import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 5. 컨트롤러
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final JWTUtil jwtUtil;
    private final NotificationService notificationService;
    
    @GetMapping("/notifications/subscribe")
    public SseEmitter subscribe(
            @RequestHeader("Authorization") String accessToken
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        return notificationService.subscribe(userId);
    }

}