package com.ssafy.exhi.domain.todo.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

        @NotNull
        private Gender gender;

        @NotNull
        private ServiceType serviceType;

    }

}
