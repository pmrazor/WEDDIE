package com.ssafy.exhi.domain.couple.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.converter.CoupleConverter;
import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse.SimpleResultDTO;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CoupleServiceImpl implements CoupleService {

    private final NotificationRepository notificationRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;

    @Override
    public CoupleResponse.SimpleResultDTO createCouple(CoupleRequest.CreateDTO coupleDTO) {
        User user = findUserByUserId(coupleDTO.getUserId());
        User opposite = findUserByUserId(coupleDTO.getOppositeId());

        Couple couple = CoupleConverter.toEntity(coupleDTO, user, opposite);
        Couple saved = coupleRepository.save(couple);
        return CoupleConverter.toSimpleDTO(saved);
    }

    @Override
    public SimpleResultDTO createCouple(Integer userId, Integer requestId) {
        // 알림이 종료된 상태인 경우 예외 발생
        CoupleMatchingNotification notification = findNotificationByRequestId(requestId);
        validateNotification(notification);
        validateCouple(notification);

        Couple couple = CoupleConverter.toEntity(notification);
        Couple saved = coupleRepository.save(couple);

        return CoupleConverter.toSimpleDTO(saved);
    }

    @Override
    public CoupleResponse.DetailResultDTO updateCouple(Integer userId, CoupleRequest.UpdateDTO updateDTO) {
        Couple couple = findCoupleByUserId(userId);

        couple.updateCouple(updateDTO);
        log.info("{}", couple);
        return CoupleConverter.toDetailDTO(couple);
    }

    private void validateCouple(CoupleMatchingNotification notification) {
        User sender = notification.getSender();
        User receiver = notification.getReceiver();

        if (coupleRepository.existsCoupleByUserIds(sender.getId(),
            receiver.getId())) {
            throw new ExceptionHandler(ErrorStatus.COUPLE_ALREADY_EXIST);
        }

        validateUsers(sender, receiver);
    }

    private CoupleMatchingNotification findNotificationByRequestId(Integer requestId) {
        return notificationRepository.findCoupleMatchingNotificationById(requestId)
            .orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.NOTIFICATION_NOT_FOUND)
            );
    }

    private void validateNotification(CoupleMatchingNotification notification) {
        if (!notification.getMatchingStatus().equals(MatchingStatus.PENDING)) {
            throw new ExceptionHandler(ErrorStatus.NOTIFICATION_STATUS_ERROR);
        }
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

    @Override
    public CoupleResponse.DetailResultDTO getCouple(Integer userId) {
        Couple couple = findCoupleByUserId(userId);

        return CoupleConverter.toDetailDTO(couple);
    }

    @Override
    public void deleteCouple(Integer userId) {
        coupleRepository.delete(findCoupleByUserId(userId));
    }

    private User findUserByUserId(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
            );
    }

    private Couple findCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId)
            .orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND)
            );
    }
}
