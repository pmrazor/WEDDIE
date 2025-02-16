package com.ssafy.exhi.domain.notification.converter;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.entity.CoupleMail;
import com.ssafy.exhi.domain.notification.model.dto.NotificationRequest;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.NotificationDTO;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.PageDTO;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMailNotification;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.notification.model.entity.Notification;
import com.ssafy.exhi.domain.notification.model.entity.NotificationType;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.exception.ExceptionHandler;
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

    public static CoupleMailNotification toEntity(
            CoupleMailRequest.CreateDTO dto, User receiver, User sender, CoupleMail coupleMail
    ) {

        return CoupleMailNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .title(dto.getTitle())
                .isRead(false)
                .notificationType(NotificationType.MAIL)
                .coupleMail(coupleMail)
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

    public static NotificationResponse.MailDTO toDTO(CoupleMailNotification notification) {
        UserResponse.SimpleResultDTO sender = Optional.ofNullable(notification.getSender())
                .map(UserConverter::toSimpleResultDTO)
                .orElse(null);
        UserResponse.SimpleResultDTO receiver = Optional.ofNullable(notification.getReceiver())
                .map(UserConverter::toSimpleResultDTO)
                .orElse(null);

        Integer id = notification.getId();

        return NotificationResponse.MailDTO.builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .mailId(notification.getCoupleMail().getId())
                .title(notification.getTitle())
                .isRead(notification.isRead())
                .notificationType(notification.getNotificationType())
                .build();
    }

    private static NotificationDTO convertToDTO(Notification notification) {
        if (notification instanceof CoupleMailNotification no) {
            return toDTO(no);
        }
        if (notification instanceof CoupleMatchingNotification no) {
            return toDTO(no);
        }

        throw new ExceptionHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    public static PageDTO toPageDTO(Page<Notification> page) {
        List<NotificationDTO> dtoList = page.getContent().stream().map(
                NotificationConverter::convertToDTO
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
/*

 */