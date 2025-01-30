package com.ssafy.exhi.domain.couple.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.converter.CoupleConverter;
import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.notification.CoupleNotificationRepository;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CoupleServiceImpl implements CoupleService {
    private final CoupleNotificationRepository coupleNotificationRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;

    @Override
    public CoupleResponse.SimpleResultDTO createCouple(CoupleRequest.CreateDTO coupleDTO) {
        User user = findUserByUserId(coupleDTO.getUserId());
        User opposite = findUserByUserId(coupleDTO.getCoupleId());

        Couple couple = CoupleConverter.toEntity(coupleDTO, user, opposite);
        Couple saved = coupleRepository.save(couple);
        return CoupleConverter.toSimpleDTO(saved);
    }

    @Override
    public CoupleResponse.SimpleResultDTO createCouple(Integer requestId, CoupleRequest.CreateDTO coupleDTO) {
        CoupleMatchingNotification notification = coupleNotificationRepository.findCoupleMatchingNotificationById(requestId)
                .orElseThrow(
                        () -> new ExceptionHandler(ErrorStatus.NOTIFICATION_NOT_FOUND)
                );

        Couple couple = CoupleConverter.toEntity(coupleDTO, notification.getReceiver(), notification.getSender());
        Couple saved = coupleRepository.save(couple);

        return CoupleConverter.toSimpleDTO(saved);
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
