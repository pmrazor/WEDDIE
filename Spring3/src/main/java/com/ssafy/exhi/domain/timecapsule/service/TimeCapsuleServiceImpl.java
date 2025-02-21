package com.ssafy.exhi.domain.timecapsule.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.TimeWrapper;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.notice.service.MinioImageService;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import com.ssafy.exhi.domain.plan.repository.PlanRepository;
import com.ssafy.exhi.domain.timecapsule.converter.TimeCapsuleConverter;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleRequest;
import com.ssafy.exhi.domain.timecapsule.model.dto.TimeCapsuleResponse;
import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import com.ssafy.exhi.domain.timecapsule.repository.TimeCapsuleRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TimeCapsuleServiceImpl implements TimeCapsuleService {

    private final TimeCapsuleRepository timeCapsuleRepository;
    private final MinioImageService minioImageService;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final CoupleRepository coupleRepository;
    private final TimeWrapper timeWrapper;

    @Override
    public TimeCapsuleResponse.TimeCapsuleResponseDto createTimeCapsule(TimeCapsuleRequest.CreateDTO registTimeCapsule) {
        User user = findUserByUserId(registTimeCapsule.getUserId());

        log.info("{}" , registTimeCapsule.getGoodImageFile());

        if(!(registTimeCapsule.getGoodImageFile() == null)) registTimeCapsule.setGoodImage(minioImageService.uploadImage(registTimeCapsule.getGoodImageFile()));
        if(!(registTimeCapsule.getBadImageFile() == null)) registTimeCapsule.setBadImage(minioImageService.uploadImage(registTimeCapsule.getBadImageFile()));

        Plan plan = planRepository.findById(registTimeCapsule.getPlanId())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.PLAN_NOT_FOUND));

        plan.validateCreateTimeCapsule(timeWrapper);

        Couple couple = findCoupleByUserId(user.getId());
        registTimeCapsule.setCoupleId(couple.getId());

        // 이미 해당 유저가 해당 플랜에 작성한 타임캡슐이 있다면
        if ( timeCapsuleRepository.existsByUserIdAndPlanId(registTimeCapsule.getUserId(), registTimeCapsule.getPlanId())) {
            throw new ExceptionHandler(ErrorStatus.TIMECAPSULE_ALREADY_EXIST);
        }

        TimeCapsule timeCapsule = TimeCapsuleConverter.toEntity(registTimeCapsule, plan, PlanStatus.FINISHED);
        TimeCapsule savedTimeCapsule = timeCapsuleRepository.save(timeCapsule);

        // 플랜 상태 업데이트
        plan.getTimeCapsules().add(savedTimeCapsule);
        plan.updateTimeCapsuleStatus();
        planRepository.save(plan);

        return TimeCapsuleConverter.toResponse(savedTimeCapsule);
    }

    // 커플 당 하나의 버진로드 가지니까 커플 아이디로 조회한 모든 캡슐 가져오면 되겠지?
    @Override
    public List<TimeCapsuleResponse.TimeCapsuleResponseDto> findTimeCapsulesByCoupleId(Integer userId) {
        User user = findUserByUserId(userId);

        Couple couple = findCoupleByUserId(userId);

        List<TimeCapsule> timeCapsules = timeCapsuleRepository.findAllByCoupleId(couple.getId());

        timeCapsules.forEach(timeCapsule -> {
            if (timeCapsule.getBadImage() != null) {
                timeCapsule.setBadImagePath(minioImageService.getImageUrl(timeCapsule.getBadImage()));
            }
            if (timeCapsule.getGoodImage() != null) {
                timeCapsule.setGoodImagePath(minioImageService.getImageUrl(timeCapsule.getGoodImage()));
            }
        });

        return TimeCapsuleConverter.toResponse(timeCapsules);
    }
    private User findUserByUserId(Integer userId) {
        return userRepository.findUserById(userId).
                orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }
    private Couple findCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
    }
}

