package com.ssafy.exhi.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class UserResponse {

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Integer id;

        @Schema(description = "사용자가 속한 부서", example = "Development")
        private String department;

        @Schema(description = "사용자의 직책", example = "Software Engineer")
        private String position;

        @Schema(description = "사용자의 이름", example = "John Doe")
        private String name;

        @Schema(description = "사용자의 아이디", example = "john123")
        private String userId;

        @Schema(description = "사용자 생성 시간", example = "2023-01-01T12:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "사용자 정보 마지막 업데이트 시간", example = "2023-01-10T15:30:00")
        private LocalDateTime updatedAt;
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
        private String userId;

        @Schema(description = "사용자 생성 시간", example = "2023-01-01T12:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "사용자 정보 마지막 업데이트 시간", example = "2023-01-10T15:30:00")
        private LocalDateTime updatedAt;
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

}
