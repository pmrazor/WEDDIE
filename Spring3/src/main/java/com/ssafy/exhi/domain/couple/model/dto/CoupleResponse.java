package com.ssafy.exhi.domain.couple.model.dto;

import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResultDTO {
        private Integer id;
        private String coupleName;
        private UserResponse.DetailResultDTO female;
        private UserResponse.DetailResultDTO male;
        private LocalDate marriageDate;
        private LocalDate loveAnniversary;
        private Integer budget;
    }
}
