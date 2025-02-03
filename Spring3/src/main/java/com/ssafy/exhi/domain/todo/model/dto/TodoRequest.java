package com.ssafy.exhi.domain.todo.model.dto;

import com.ssafy.exhi.domain.ServiceType;
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
        private ServiceType serviceType;
        private boolean isCompleted;

    }


}
