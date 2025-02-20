package com.ssafy.exhi.domain.reservation.model.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReservationRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        @NotNull
        Integer shopId;

        @NotNull
        Integer itemId;

        List<OptionDTO> options;

        @NotNull
        LocalDateTime reservationTime;
    }

    @Getter
    public static class OptionDTO {
        Integer optionId;
        Integer quantity;
    }

}
