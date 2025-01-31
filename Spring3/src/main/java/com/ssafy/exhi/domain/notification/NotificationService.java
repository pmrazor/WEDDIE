package com.ssafy.exhi.domain.notification;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.notification.converter.NotificationConverter;
import com.ssafy.exhi.domain.notification.model.dto.NotificationRequest;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    // 각 사용자별 SSE Emitter를 저장
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final CoupleNotificationRepository coupleNotificationRepository;
    private final UserRepository userRepository;

    // 사용자가 접속할 때 호출되는 메서드
    public SseEmitter subscribe(Integer userId) {
        // 1시간 동안 연결 유지
        SseEmitter emitter = new SseEmitter(3600000L);
        
        // 연결이 종료될 때 정리
        emitter.onCompletion(() -> {
            emitters.remove(userId);
        });
        
        // 타임아웃 시 정리
        emitter.onTimeout(() -> {
            emitters.remove(userId);
        });
        
        // 에러 발생 시 정리
        emitter.onError(ex -> {
            emitters.remove(userId);
        });
        
        // 기존 연결이 있다면 제거
        SseEmitter oldEmitter = emitters.get(userId);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }
        
        emitters.put(userId, emitter);
        
        // 연결 즉시 테스트 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("연결되었습니다!"));
        } catch (IOException e) {
            emitters.remove(userId);
        }
        
        return emitter;
    }
    
    // 특정 사용자에게 알림을 보내는 메서드
    public void sendCoupleNotification(NotificationRequest.CoupleMatchingDTO notificationDTO, MatchingStatus status) {
        User receiver = getUserById(notificationDTO.getReceiverId());
        User sender = getUserById(notificationDTO.getSenderId());
        log.info("emmiter size -> {}, 보낸 사람 -> {}, 받는 사람 -> {}", emitters.size(), sender.getName(), receiver.getName());
        SseEmitter emitter = Optional.ofNullable(emitters.get(receiver.getId())).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_BAD_REQUEST)
        );

        // 알림 리포지토리 저장
        CoupleMatchingNotification entity = NotificationConverter.toEntity(notificationDTO, receiver, sender, status);
        CoupleMatchingNotification saved = coupleNotificationRepository.save(entity);

        // 알림 전송
        NotificationResponse.CoupleMatchingDTO payload = NotificationConverter.toDTO(saved);
        try {
            emitter.send(SseEmitter.event()
                    .name(payload.getNotificationType().toString())
                    .data(payload));

        } catch (IOException e) {
            throw new ExceptionHandler(ErrorStatus.USER_BAD_REQUEST);
        }
    }

    private User getUserById(Integer userId) {
        return userRepository.findUserById(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
        );
    }
}
