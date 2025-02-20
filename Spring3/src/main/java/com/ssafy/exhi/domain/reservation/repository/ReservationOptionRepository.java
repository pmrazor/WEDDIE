package com.ssafy.exhi.domain.reservation.repository;

import com.ssafy.exhi.domain.reservation.model.entity.ReservationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationOptionRepository extends JpaRepository<ReservationOption, Integer> {
}
