package com.ssafy.exhi.domain.notification.model.dto;

import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
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
    public static class CoupleMatchingDTO {
        private final static String url = "/api/couple/request";
        private Integer id;
        private UserResponse.SimpleResultDTO receiver;
        private UserResponse.SimpleResultDTO sender;
        private String message;
        private boolean isRead;
        private NotificationType notificationType;
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
        private List<NotificationResponse.CoupleMatchingDTO> contents;
        private Integer listSize;
        private boolean isFirstPage;
        private boolean isLastPage;
        private Integer totalPages;
        private Long totalElements;
    }

}
