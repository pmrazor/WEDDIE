package com.ssafy.exhi.domain.notice.model.dto;

import com.ssafy.exhi.domain.notice.model.entity.NoticeCategory;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class NoticeResponse {

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResultDTO {

        private Integer id;
        private String title;
        private String content;
        private NoticeCategory category;
        private UserResponse.SimpleResultDTO user;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {

        private Integer id;
        private String title;
        private String content;
        private NoticeCategory category;
        private UserResponse.SimpleResultDTO user;
        private String imageUrl;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageDTO {

        private List<SimpleResultDTO> contents;
        private Integer listSize;
        private boolean isFirstPage;
        private boolean isLastPage;
        private Integer totalPages;
        private Long totalElements;

    }

}
