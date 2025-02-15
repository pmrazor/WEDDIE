package com.ssafy.exhi.domain.todo.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.todo.model.entity.StageType;
import com.ssafy.exhi.domain.todo.model.entity.TodoType;
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

        private boolean together;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTodosDTO {
        private Integer planId;

        @NotBlank
        private String content;
        private StageType stageType;
        private boolean together;
        private ServiceType serviceType;
        private TodoType todoType;
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

        private boolean together;

        private TodoType todoType;
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
