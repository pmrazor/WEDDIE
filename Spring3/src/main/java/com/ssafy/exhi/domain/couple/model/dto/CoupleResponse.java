package com.ssafy.exhi.domain.couple.model.dto;

import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CoupleResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResultDTO {
        private Integer id;
        private String coupleName;
        private UserResponse.SimpleResultDTO female;
        private UserResponse.SimpleResultDTO male;

    }
}
