package com.ssafy.exhi.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

public class UserRequest {

    @Data
    @Builder
    @Schema(name = "로그인 DTO", description = "로그인 요청 객체")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginDTO {

        @Schema(
                title = "사용자 ID",
                description = "로그인에 사용되는 사용자 ID",
                example = "user123",
                required = true
        )
        @NotNull(message = "Login ID cannot be null")
        private String userId;

        @Schema(
                title = "비밀번호",
                description = "9-16자의 영문, 숫자, 특수문자를 포함한 비밀번호",
                example = "Test123!@#",
                required = true,
                minLength = 9,
                maxLength = 16
        )
        @NotBlank(message = "Password is required.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,16}$",
                message = "Password must be between 9 and 16 characters long, and include letters, numbers, and special characters."
        )
        private String password;
    }

    @Data
    @Builder
    @ToString
    @Schema(name = "회원가입 DTO", description = "회원가입 요청 객체")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpDTO {
        @Schema(
                title = "사용자 ID",
                description = "4-16자의 로그인 ID",
                example = "user123",
                required = true,
                minLength = 4,
                maxLength = 16
        )
        @NotNull(message = "Login ID cannot be null")
        @Size(min = 4, max = 16, message = "Login ID must be between 4 and 16 characters")
        private String userId;

        @Schema(
                title = "비밀번호",
                description = "9-16자의 영문, 숫자, 특수문자를 포함한 비밀번호",
                example = "Test123!@#",
                required = true,
                minLength = 9,
                maxLength = 16
        )
        @NotBlank(message = "Password is required.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,16}$",
                message = "Password must be between 9 and 16 characters long, and include letters, numbers, and special characters."
        )
        private String password;

        @Schema(
                title = "이름",
                description = "사용자 이름 (최대 30자)",
                example = "홍길동",
                required = true,
                maxLength = 30
        )
        @NotNull(message = "name cannot be null")
        @Size(max = 30, message = "name 30 characters")
        private String name;

        @Schema(
                title = "부서",
                description = "소속 부서 (최대 30자)",
                example = "개발팀",
                maxLength = 30
        )
        @Column(name = "department")
        @Size(max = 30, message = "department 30 characters")
        private String department;

        @Schema(
                title = "직급",
                description = "직급 정보",
                example = "선임연구원"
        )
        @Column(name = "position")
        private String position;
    }

    @Data
    @Builder
    @ToString
    @Schema(name = "회원정보 수정 DTO", description = "회원정보 수정 요청 객체")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        @Schema(
                title = "사용자 ID (PK)",
                description = "수정할 사용자의 고유 식별자",
                example = "1"
        )
        private Integer id;

        @Schema(
                title = "현재 비밀번호",
                description = "9-16자의 영문, 숫자, 특수문자를 포함한 현재 비밀번호",
                example = "Current123!@#",
                required = true,
                minLength = 9,
                maxLength = 16
        )
        @NotBlank(message = "Password is required.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,16}$",
                message = "Password must be between 9 and 16 characters long, and include letters, numbers, and special characters."
        )
        private String oldPassword;

        @Schema(
                title = "새 비밀번호",
                description = "9-16자의 영문, 숫자, 특수문자를 포함한 새 비밀번호",
                example = "New123!@#",
                required = true,
                minLength = 9,
                maxLength = 16
        )
        @NotBlank(message = "Password is required.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,16}$",
                message = "Password must be between 9 and 16 characters long, and include letters, numbers, and special characters."
        )
        private String newPassword;

        @Schema(
                title = "이름",
                description = "수정할 사용자 이름 (최대 30자)",
                example = "홍길동",
                required = true,
                maxLength = 30
        )
        @NotNull(message = "name cannot be null")
        @Size(max = 30, message = "name must be between 3 and 30 characters")
        private String name;

        @Schema(
                title = "부서",
                description = "수정할 소속 부서 (최대 30자)",
                example = "개발팀",
                maxLength = 30
        )
        @Column(name = "department")
        @Size(max = 30, message = "department must be between 4 and 30 characters")
        private String department;

        @Schema(
                title = "직급",
                description = "수정할 직급 정보",
                example = "책임연구원"
        )
        @Column(name = "position")
        private String position;
    }


}
