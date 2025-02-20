package com.ssafy.exhi.domain.reservation.repository;

import com.ssafy.exhi.domain.reservation.model.entity.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationItemRepository extends JpaRepository<ReservationItem, Integer> {
}
