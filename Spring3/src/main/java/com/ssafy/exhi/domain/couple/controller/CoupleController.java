package com.ssafy.exhi.domain.couple.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.service.CoupleService;
import com.ssafy.exhi.domain.notification.model.dto.NotificationRequest;
import com.ssafy.exhi.domain.notification.model.dto.NotificationResponse.CoupleMatchingDTO;
import com.ssafy.exhi.domain.notification.model.entity.MatchingStatus;
import com.ssafy.exhi.domain.notification.service.NotificationService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.ssafy.exhi.domain.couple.model.dto.CoupleResponse.SimpleResultDTO;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/couple")
public class CoupleController {

    private final JWTUtil jwtUtil;
    private final CoupleService coupleService;
    private final NotificationService notificationService;

    /**
     * 커플 알림 기능 개발전까지 사용할 임시 API
     *
     * @param accessToken
     * @param coupleDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createCouple(
        @RequestHeader("Authorization") String accessToken,
        @RequestBody @Valid CoupleRequest.CreateDTO coupleDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("createCouple: userId={}", userId);

        coupleDTO.setUserId(userId);

        SimpleResultDTO response = coupleService.createCouple(coupleDTO);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestCouple(
        @RequestHeader("Authorization") String accessToken,
        @RequestBody @Valid NotificationRequest.CoupleMatchingDTO matchingDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        matchingDTO.setSenderId(userId);
        log.info("requestCouple: sender={}, receiver={}", userId, matchingDTO.getReceiverId());

        CoupleMatchingDTO response = notificationService.sendCoupleRequest(matchingDTO);

        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    /**
     * TODO: 커플 알람 기능이 완성되면 사용할 커플 요청 수락 API
     *
     * @param accessToken
     * @param requestId
     * @return
     */
    @PostMapping("/request/{requestId}/approved")
    public ResponseEntity<?> approveCoupleRequest(
        @RequestHeader("Authorization") String accessToken,
        @PathVariable(name = "requestId") Integer requestId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("createCouple: userId={}", userId);

        return getSimpleResultDTOResponseEntity(userId, requestId, MatchingStatus.APPROVED);
    }

    /**
     * TODO: 커플 알람 기능이 완성되면 사용할 커플 요청 거절 API
     *
     * @param accessToken
     * @param requestId
     * @return
     */
    @PostMapping("/request/{requestId}/rejected")
    public ResponseEntity<?> rejectCoupleRequest(
        @RequestHeader("Authorization") String accessToken,
        @PathVariable(name = "requestId") Integer requestId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("rejectCoupleRequest: userId={}", userId);

        return getSimpleResultDTOResponseEntity(userId, requestId, MatchingStatus.REJECTED);
    }

    private ResponseEntity<?> getSimpleResultDTOResponseEntity(
        Integer userId,
        Integer requestId,
        MatchingStatus status
    ) {

        SimpleResultDTO response = SimpleResultDTO.builder().build();

        if (status == MatchingStatus.APPROVED) {
            response = coupleService.createCouple(userId, requestId);
        }

        notificationService.sendCoupleResponse(requestId, status);

        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    /**
     * 커플 조회 오류 - 커플을 아직 맺지 않은 경우
     *
     * @param accessToken
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getCouple(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("getCouple: userId={}", userId);

        CoupleResponse.DetailResultDTO response = coupleService.getCouple(userId);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCouple(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("deleteCouple: userId={}", userId);

        coupleService.deleteCouple(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<?> updateCouple(
        @RequestHeader("Authorization") String accessToken,
        @RequestBody CoupleRequest.UpdateDTO updateDTO) {
        Integer userId = jwtUtil.getUserId(accessToken);

        CoupleResponse.DetailResultDTO couple = coupleService.updateCouple(userId, updateDTO);
        return ResponseEntity.ok().body(couple);
    }
}
