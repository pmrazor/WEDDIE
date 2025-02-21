package com.ssafy.exhi.domain.user.model.dto;

import com.ssafy.exhi.domain.oauth.AuthProvider;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class UserRequest {

    @Data
    @Builder
    @Schema(name = "로그인 DTO", description = "로그인 요청 객체")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginDTO {

        @NotNull(message = "Login ID cannot be null")
        private String loginId;

        @NotBlank(message = "Password is required.")
        private String password;
    }

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpDTO {

        @NotNull(message = "Login ID cannot be null")
        private String loginId;

        private String password;

        private String name;

        private Gender gender;

        @Column(name = "provider")
        private AuthProvider provider;

        @Column(name = "profile_img")
        private String profileImg;
    }

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {

        private Integer id;

        private String oldPassword;

        private String newPassword;

        private String name;

        @Column(name="profile_img")
        private String profileImg;
    }

    // 로컬 유저 기본 정보 수정
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateInfoDTO {
        @NotBlank
        private String name;

        @NotBlank
        private String profileImg;
    }

    // 유저 상세 정보 수정
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDetailDTO {
        @NotNull
        private Integer age;

        @NotNull
        private Gender gender;

        @NotBlank
        private String phone;

        @NotBlank
        private String mbti;
    }

    // 유저 상세 정보 생성
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDetailDTO {
        @NotNull
        private Integer age;

        @NotNull
        private Gender gender;

        @NotBlank
        private String phone;

        @NotBlank
        private String mbti;
    }

    // 유저 상세 정보 조회
    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getDetailDTO {
        @NotNull
        private Integer age;

        @NotNull
        private Gender gender;

        @NotBlank
        private String phone;

        @NotBlank
        private String mbti;
    }

}
