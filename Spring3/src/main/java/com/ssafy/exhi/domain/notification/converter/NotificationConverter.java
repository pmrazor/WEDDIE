package com.ssafy.exhi.domain.notification.converter;

import com.ssafy.exhi.domain.notification.model.dto.NotificationRequest;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.entity.User;

import java.util.Optional;


public class NotificationConverter {

    public static CoupleMatchingNotification toEntity(NotificationRequest.CoupleMatchingDTO dto, User receiver, User sender, MatchingStatus status) {

        return CoupleMatchingNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .message("커플 신청")
                .isRead(false)
                .matchingStatus(status)
                .notificationType(dto.getNotificationType())
                .build();
    }

    public static NotificationResponse.CoupleMatchingDTO toDTO(CoupleMatchingNotification notification) {
        UserResponse.SimpleResultDTO sender = Optional.ofNullable(notification.getSender())
                .map(UserConverter::toSimpleResultDTO)
                .orElse(null);
        UserResponse.SimpleResultDTO receiver = Optional.ofNullable(notification.getReceiver())
                .map(UserConverter::toSimpleResultDTO)
                .orElse(null);

        Integer id = notification.getId();

        return NotificationResponse.CoupleMatchingDTO.builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .notificationType(notification.getNotificationType())
                .matchingStatus(notification.getMatchingStatus())
                .approvedUrl(NotificationResponse.CoupleMatchingDTO.getApprovedUrl(id))
                .rejectedUrl(NotificationResponse.CoupleMatchingDTO.getRejectedUrl(id))
                .build();
    }
}
