package com.ssafy.exhi.domain.todo.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DefaultTodoResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        private Integer defaultTodoId;

        private ServiceType serviceType;

        private String content;
    }
}
