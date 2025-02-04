package com.ssafy.exhi.domain.todo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TodoRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        private Integer planId;

        @NotBlank
        private String content;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private Integer planId;

        private Integer todoId;

        @NotBlank
        private String content;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckDTO {
        private Integer planId;

        private Integer todoId;

        @NotNull(message = "필드 확인 (isCompleted) is required")
        private Boolean isCompleted;

    }


}
