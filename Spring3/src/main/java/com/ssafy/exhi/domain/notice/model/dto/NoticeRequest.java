package com.ssafy.exhi.domain.notice.model.dto;

import com.ssafy.exhi.domain.notice.model.entity.NoticeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

public class NoticeRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {

        private String title;
        private String content;
        private Integer userId;
        private NoticeCategory category;
        private MultipartFile image;

    }
}
