package com.ssafy.exhi.domain.reservation.repository;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.reservation.model.entity.Reservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    Reservation getReservationById(Integer reservationId);

    List<Reservation> findAllByCouple(Couple couple);
}
