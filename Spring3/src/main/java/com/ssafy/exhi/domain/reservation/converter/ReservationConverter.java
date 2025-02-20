package com.ssafy.exhi.domain.reservation.converter;

import com.ssafy.exhi.domain.reservation.model.dto.ReservationRequest;
import com.ssafy.exhi.domain.reservation.model.entity.Reservation;

public class ReservationConverter {
    public static Reservation toEntity(ReservationRequest.CreateDTO createDTO) {
        return Reservation.builder()
                .build();
    }
}
