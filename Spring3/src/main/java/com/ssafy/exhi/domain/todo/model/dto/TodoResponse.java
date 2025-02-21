package com.ssafy.exhi.domain.todo.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import java.util.List;
import java.util.Map;

public class TodoResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        private Integer todoId;

        private ServiceType serviceType;

        private String content;

        private boolean isCompleted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindResultDTO {
        private Integer todoId;
        private String content;
        private boolean isCompleted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindAllResultDTO {
        private Integer planId;
        private ServiceType serviceType;
        private Map<String, List<FindResultDTO>> todos;
        private String tip;
    }
}
