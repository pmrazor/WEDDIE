package com.ssafy.exhi.domain.notification.converter;

import com.ssafy.exhi.domain.notification.model.dto.NotificationRequest;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.PageDTO;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.notification.model.entity.NotificationType;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public class NotificationConverter {

    public static CoupleMatchingNotification toEntity(
            NotificationRequest.CoupleMatchingDTO dto, User receiver, User sender
    ) {

        return CoupleMatchingNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .message(dto.getMessage())
                .isRead(false)
                .matchingStatus(MatchingStatus.PENDING)
                .notificationType(NotificationType.COUPLE_MATCHING)
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

    public static PageDTO toPageDTO(Page<CoupleMatchingNotification> page) {
        List<NotificationResponse.CoupleMatchingDTO> dtoList = page.getContent().stream().map(
                NotificationConverter::toDTO
        ).toList();

        return NotificationResponse.PageDTO.builder()
                .contents(dtoList)
                .listSize(dtoList.size())
                .isFirstPage(page.isFirst())
                .isLastPage(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

}
