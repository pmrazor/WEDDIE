package com.ssafy.exhi.domain.notification.repository;

import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.user.model.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoupleNotificationRepository extends JpaRepository<CoupleMatchingNotification, Integer> {

    Optional<CoupleMatchingNotification> findCoupleMatchingNotificationById(Integer requestId);

    Page<CoupleMatchingNotification> findNotificationByReceiver(User receiver, Pageable pageable);
}
