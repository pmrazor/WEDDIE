package com.ssafy.exhi.domain.todo.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DefaultTodoRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {

        @NotBlank
        private String content;

        @NotBlank
        private ServiceType serviceType;

    }

}
