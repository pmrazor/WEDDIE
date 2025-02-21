package com.ssafy.exhi.domain.reservation.service;

import com.ssafy.exhi.domain.reservation.model.dto.ReservationRequest;
import com.ssafy.exhi.domain.reservation.model.entity.Reservation;
import java.util.List;

public interface ReservationService {
    List<Reservation> getAllReservations(Integer userId);

    Reservation getReservation(Integer userId, Integer reservationId);

    Reservation save(Integer userId, ReservationRequest.CreateDTO createDTO);
}
