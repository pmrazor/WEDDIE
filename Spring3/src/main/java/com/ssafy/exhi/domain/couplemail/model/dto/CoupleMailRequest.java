package com.ssafy.exhi.domain.couplemail.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 메일 관련 요청/응답 DTO - Request: 메일 작성 및 업데이트 요청 - Response: 메일 조회 결과
 */
public class CoupleMailRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        private Integer userId;  // 발신자 ID
        private Integer mailId;
        private String title;
        private String content;
        private Integer templateId;  // 선택한 템플릿 ID
        private MultipartFile attachment;  // 첨부 이미지
    }
}
