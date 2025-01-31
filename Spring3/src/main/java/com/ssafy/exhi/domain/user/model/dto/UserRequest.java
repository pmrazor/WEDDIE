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


}
