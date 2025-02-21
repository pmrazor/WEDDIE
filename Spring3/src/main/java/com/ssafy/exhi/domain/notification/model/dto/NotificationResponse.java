package com.ssafy.exhi.domain.notification.model.dto;

import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.notification.model.entity.NotificationPayload;
import com.ssafy.exhi.domain.notification.model.entity.NotificationType;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

public class NotificationResponse {

    @Data
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDTO implements NotificationPayload {
        private Integer id;
        private UserResponse.SimpleResultDTO receiver;
        private UserResponse.SimpleResultDTO sender;
        private boolean isRead;
        private NotificationType notificationType;

        @Override
        public String getNotificationType() {
            return notificationType.toString();
        }

    }

    @Data
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MailDTO extends NotificationDTO {
        private Integer mailId;
        private String title;
    }

    @Data
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoupleMatchingDTO extends NotificationDTO {
        private final static String url = "/api/couple/request";
        private String message;
        private MatchingStatus matchingStatus;
        private String approvedUrl;
        private String rejectedUrl;

        public static String getApprovedUrl(Integer id) {
            return url + "/" + id + "/approved";
        }

        public static String getRejectedUrl(Integer id) {
            return url + "/" + id + "/rejected";
        }
    }

    @Data
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageDTO {
        private List<NotificationDTO> contents;
        private Integer listSize;
        private boolean isFirstPage;
        private boolean isLastPage;
        private Integer totalPages;
        private Long totalElements;
    }

}
