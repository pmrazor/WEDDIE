package com.ssafy.exhi.domain.notification.repository;

import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.notification.model.entity.Notification;
import com.ssafy.exhi.domain.user.model.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Query("SELECT n FROM CoupleMatchingNotification n WHERE n.id = :requestId")
    Optional<CoupleMatchingNotification> findCoupleMatchingNotificationById(@Param("requestId") Integer requestId);

    @Query("SELECT n FROM Notification n WHERE n.receiver = :receiver")
    Page<Notification> findNotificationByReceiver(@Param("receiver") User receiver, Pageable pageable);
}
