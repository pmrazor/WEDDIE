package com.ssafy.exhi.domain.notification.model.dto;

import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.notification.model.entity.NotificationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
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
        @NotNull
        private Integer receiverId;

        private Integer senderId;

        @Size(max = 255)
        private String message;

        private boolean isRead;

        private MatchingStatus matchingStatus;

        private NotificationType notificationType;
    }

    @Data
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MailDTO {
        @NotNull
        private Integer receiverId;

        private Integer senderId;

        private String title;

        private boolean isRead;

        private NotificationType notificationType;
    }

}