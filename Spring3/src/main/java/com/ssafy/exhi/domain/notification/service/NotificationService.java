package com.ssafy.exhi.domain.notification.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest.CreateDTO;
import com.ssafy.exhi.domain.couplemail.model.entity.CoupleMail;
import com.ssafy.exhi.domain.couplemail.repository.CoupleMailRepository;
import com.ssafy.exhi.domain.notification.converter.NotificationConverter;
import com.ssafy.exhi.domain.notification.model.dto.NotificationRequest;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.CoupleMatchingDTO;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.MailDTO;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.PageDTO;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMailNotification;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.notification.model.entity.Notification;
import com.ssafy.exhi.domain.notification.model.entity.SseEmitterManager;
import com.ssafy.exhi.domain.notification.repository.NotificationRepository;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SseEmitterManager sseEmitterManager;
    private final NotificationRepository notificationRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final CoupleMailRepository coupleMailRepository;

    public SseEmitter subscribe(Integer userId) {
        return sseEmitterManager.subscribe(userId);
    }

    @Transactional
    public MailDTO sendCoupleMailNotification(CoupleMailRequest.CreateDTO mailDTO) {
        // 1. 발신자 정보 조회 및 검증
        User sender = findUserByUserId(mailDTO.getUserId());
        Couple couple = findCoupleByUserId(mailDTO.getUserId());
        CoupleMail mail = findMailWithAuth(mailDTO, sender);

        // 2. 수신자 결정 (커플 중 발신자가 아닌 사람)
        User receiver = sender.equals(couple.getMale()) ?
                couple.getFemale() : couple.getMale();

        CoupleMailNotification entity = NotificationConverter.toEntity(mailDTO, receiver, sender, mail);
        CoupleMailNotification saved = notificationRepository.save(entity);

        MailDTO dto = NotificationConverter.toDTO(saved);
        sseEmitterManager.sendNotification(receiver.getId(), dto);
        return dto;
    }

    private CoupleMail findMailWithAuth(CreateDTO mailDTO, User sender) {
        return coupleMailRepository.findMailWithAuth(mailDTO.getMailId(), sender.getId()).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.MAIL_NOT_FOUND)
        );
    }

    private Couple findCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
    }

    @Transactional
    public CoupleMatchingDTO sendCoupleRequest(
            NotificationRequest.CoupleMatchingDTO notificationDTO) {
        User receiver = getUserById(notificationDTO.getReceiverId());
        User sender = getUserById(notificationDTO.getSenderId());
        validateUsers(receiver, sender);

        CoupleMatchingNotification entity = NotificationConverter.toEntity(notificationDTO, receiver, sender);
        CoupleMatchingNotification saved = notificationRepository.save(entity);

        CoupleMatchingDTO dto = NotificationConverter.toDTO(saved);
        sseEmitterManager.sendNotification(receiver.getId(), dto);
        return dto;
    }

    @Transactional
    public void sendCoupleResponse(Integer requestId, MatchingStatus status) {
        CoupleMatchingNotification notification = findNotificationByRequestId(requestId);
        validateNotification(notification);

        notification.updateMatchingStatus(status);
        User receiver = notification.getSender();

        NotificationResponse.CoupleMatchingDTO payload = NotificationConverter.toDTO(notification);
        sseEmitterManager.sendNotification(receiver.getId(), payload);
    }

    private void validateUsers(User receiver, User sender) {
        Gender receiverGender = Optional.ofNullable(receiver)
                .map(User::getUserDetail)
                .map(UserDetail::getGender)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.INVALID_USER));

        Gender senderGender = Optional.ofNullable(sender)
                .map(User::getUserDetail)
                .map(UserDetail::getGender)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.INVALID_USER));

        if (receiverGender == senderGender) {
            throw new ExceptionHandler(ErrorStatus.COUPLE_SAME_GENDERED);
        }
    }

    private CoupleMatchingNotification findNotificationByRequestId(Integer requestId) {
        return notificationRepository
                .findCoupleMatchingNotificationById(requestId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));
    }

    private User getUserById(Integer userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }

    private void validateNotification(CoupleMatchingNotification notification) {
        if (!notification.getMatchingStatus().equals(MatchingStatus.PENDING)) {
            throw new ExceptionHandler(ErrorStatus.NOTIFICATION_STATUS_ERROR);
        }
    }

    public PageDTO getNotifications(Integer userId, Pageable pageable) {
        User user = findUserByUserId(userId);
        Page<Notification> page = notificationRepository
                .findNotificationByReceiver(user, pageable);
        return NotificationConverter.toPageDTO(page);
    }

    private User findUserByUserId(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }
}