package com.ssafy.exhi.domain.notification.model.dto;

import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.notification.model.entity.NotificationType;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Builder
@RequiredArgsConstructor
public class NotificationRequest {

    @Data
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoupleMatchingDTO {
        private Integer receiverId;
        private Integer senderId;
        private String message;
        private boolean isRead;
        private NotificationType notificationType;
        private CoupleRequest.CreateDTO payload;

    }

}