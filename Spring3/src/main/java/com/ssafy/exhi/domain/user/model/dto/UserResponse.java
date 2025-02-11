package com.ssafy.exhi.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

public class UserResponse {

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Integer id;

        @Schema(description = "사용자의 이름", example = "John Doe")
        private String name;

        @Schema(description = "사용자의 아이디", example = "john123")
        private String loginId;

        @Schema(description = "사용자 생성 시간", example = "2023-01-01T12:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "사용자 정보 마지막 업데이트 시간", example = "2023-01-10T15:30:00")
        private LocalDateTime updatedAt;

        private String provider;

        private String profileImg;
    }

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResultDTO {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Integer id;

        @Schema(description = "사용자의 이름", example = "John Doe")
        private String name;

        @Schema(description = "사용자의 아이디", example = "john123")
        private String loginId;

        private String provider;
        private String profileImg;
    }

    // 회원 기본 정보 수정 API 응답용 (gender, profileimg)
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Integer id;

        @Schema(description = "사용자의 아이디", example = "john123") // ✅ loginId 필드 추가!
        private String loginId;

        @Schema(description = "사용자의 이름", example = "John Doe")
        private String name;

//        @Schema(description = "성별", example = "MALE")
//        private String gender;

        @Schema(description = "프로필 이미지", example = "https://example.com/profile.jpg")
        private String profileImg;
    }

    // 상세 정보 수정 API 응답용 (age, phone, mbti)
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetailDTO {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Integer id;

        @Schema(description = "나이", example = "25")
        private Integer age;

        @Schema(description = "성별", example = "MALE")
        private String gender;

        @Schema(description = "전화번호", example = "010-1234-5678")
        private String phone;

        @Schema(description = "MBTI", example = "INTJ")
        private String mbti;
    }

    //아이디 중복체크용
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateLoginIdDto {
        @Schema(description = "로그인 아이디 중복 체크", example = "true")
        boolean available;
    }

    @Data
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenDTO {
        @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1...")
        private String refreshToken;

        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1...")
        private String accessToken;
    }

    @Data
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageDTO {
        private List<UserResponse.SimpleResultDTO> contents;
        private Integer listSize;
        private boolean isFirstPage;
        private boolean isLastPage;
        private Integer totalPages;
        private Long totalElements;
    }

}
