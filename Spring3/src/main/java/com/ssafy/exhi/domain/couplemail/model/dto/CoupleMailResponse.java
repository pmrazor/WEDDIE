package com.ssafy.exhi.domain.couplemail.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CoupleMailResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        private Integer mailId;
        private String title;
        private String content;
        private String senderName;
        private String receiverName;
        private boolean isRead;
        private boolean isLiked;
        private String attachmentFileName;
        private LocalDateTime createdAt;
        private MailTemplateDTO template;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResultDTO {
        private Integer mailId;
        private String title;
        private String senderName;
        private boolean isRead;
        private boolean isLiked;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MailTemplateDTO {
        private Integer templateId;
        private String title;
        private String content;
        private String description;
        private ServiceType serviceType;
    }
}