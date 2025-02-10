package com.ssafy.exhi.domain.notification.model.dto;

import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.notification.model.entity.NotificationType;
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
        private Integer receiverId;
        private Integer senderId;
        private String message;
        private boolean isRead;
        private NotificationType notificationType;
        private MatchingStatus matchingStatus;
        private CoupleRequest.CreateDTO payload;

    }

}