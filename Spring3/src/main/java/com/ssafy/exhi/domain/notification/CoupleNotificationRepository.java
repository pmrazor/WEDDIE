package com.ssafy.exhi.domain.notification;

import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleNotificationRepository extends JpaRepository<CoupleMatchingNotification, Integer> {

    Optional<CoupleMatchingNotification> findCoupleMatchingNotificationById(Integer requestId);
}
