package com.ssafy.exhi.domain.notification.controller;

import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.PageDTO;
import com.ssafy.exhi.domain.notification.service.NotificationService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping("/notifications")
    public PageDTO getNotifications(
            @RequestHeader("Authorization") String accessToken,
            Pageable pageable
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        return notificationService.getNotifications(userId, pageable);
    }

}