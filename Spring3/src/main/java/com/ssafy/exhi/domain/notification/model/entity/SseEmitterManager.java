package com.ssafy.exhi.domain.notification.model.entity;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.exception.ExceptionHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterManager {
    private static final Long DEFAULT_TIMEOUT = 3600000L;  // 1시간
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Integer userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitter.onCompletion(() -> {
            log.info("SSE Connection Completed for user: {}", userId);
            removeEmitter(userId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE Connection Timeout for user: {}", userId);
            removeEmitter(userId);
        });

        emitter.onError(ex -> {
            log.error("SSE Connection Error for user: {}", userId, ex);
            removeEmitter(userId);
        });

        // 기존 연결이 있다면 제거
        removeExistingEmitter(userId);

        emitters.put(userId, emitter);
        sendConnectEvent(emitter, userId);

        return emitter;
    }

    private void removeExistingEmitter(Integer userId) {
        SseEmitter oldEmitter = emitters.get(userId);
        if (oldEmitter != null) {
            oldEmitter.complete();
            emitters.remove(userId);
        }
    }

    private void removeEmitter(Integer userId) {
        emitters.remove(userId);
    }

    private void sendConnectEvent(SseEmitter emitter, Integer userId) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("연결되었습니다!"));
        } catch (IOException e) {
            log.error("Failed to send connect event to user: {}", userId, e);
            removeEmitter(userId);
        }
    }

    public <T extends NotificationPayload> void sendNotification(Integer userId, T notification) {
        if (!emitters.containsKey(userId)) {
            return;
        }

        SseEmitter emitter = Optional.ofNullable(emitters.get(userId))
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.SSR_SUBSCRIBE_NOT_FOUND));

        try {
            emitter.send(SseEmitter.event()
                    .name(notification.getNotificationType())
                    .data(notification));
        } catch (IOException e) {
            log.error("Failed to send notification to user: {}", userId, e);
            removeEmitter(userId);
            throw new ExceptionHandler(ErrorStatus.SSR_SUBSCRIBE_NOT_FOUND);
        }
    }

    public boolean hasConnection(Integer userId) {
        return emitters.containsKey(userId);
    }
}