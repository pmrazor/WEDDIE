package com.ssafy.exhi.domain.couple.controller;

import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.service.CoupleService;
import com.ssafy.exhi.domain.notification.NotificationService;
import com.ssafy.exhi.domain.notification.model.dto.NotificationRequest;
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
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestCouple(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid NotificationRequest.CoupleMatchingDTO matchingDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        matchingDTO.setSenderId(userId);
        log.info("requestCouple: sender={}, receiver={}", userId, matchingDTO.getReceiverId());

        notificationService.sendCoupleNotification(matchingDTO);
        return ResponseEntity.ok().body(null);
    }

    /**
     * TODO: 커플 알람 기능이 완성되면 사용할 커플 요청 수락 API
     * @param accessToken
     * @param requestId
     * @param coupleDTO
     * @return
     */
    @PostMapping("/request/{requestId}/approved")
    public ResponseEntity<?> approveCoupleRequest(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable(name = "requestId") Integer requestId,
            @RequestBody @Valid CoupleRequest.CreateDTO coupleDTO

    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("createCouple: userId={}", userId);

        coupleDTO.setUserId(userId);

        SimpleResultDTO response = coupleService.createCouple(requestId, coupleDTO);
        return ResponseEntity.ok().body(response);
    }

    /**
     * TODO: 커플 알람 기능이 완성되면 사용할 커플 요청 거절 API
     *
     * @param accessToken
     * @param requestId
     * @param coupleDTO
     * @return
     */
    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectCoupleRequest(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable(name = "requestId") Integer requestId,
            @RequestBody @Valid CoupleRequest.CreateDTO coupleDTO

    ) {
//        Integer userId = jwtUtil.getUserId(accessToken);
//        log.info("createCouple: userId={}", userId);
//
//        coupleDTO.setUserId(userId);
//
//        SimpleResultDTO response = coupleService.createCouple(requestId, coupleDTO);
//        return ResponseEntity.ok().body(response);
        return null;
    }

    /**
     * 커플 조회
     * 오류
     *      - 커플을 아직 맺지 않은 경우
     * @param accessToken
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getCouple(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("getCouple: userId={}", userId);

        CoupleResponse.DetailResultDTO response = coupleService.getCouple(userId);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCouple(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("deleteCouple: userId={}", userId);

        coupleService.deleteCouple(userId);
        return ResponseEntity.noContent().build();
    }

}
