package com.ssafy.exhi.domain.couplemail.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailResponse;
import com.ssafy.exhi.domain.couplemail.service.CoupleMailService;
import com.ssafy.exhi.domain.notification.service.NotificationService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class CoupleMailController {

    private final NotificationService notificationService;
    private final CoupleMailService coupleMailService;
    private final JWTUtil jwtUtil;

    /**
     * 자신의 짝에게 메일을 생성하는 API
     *
     * @param accessToken
     * @param createDTO
     * @return
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMail(
            @RequestHeader("Authorization") String accessToken,
            @ModelAttribute @Valid CoupleMailRequest.CreateDTO createDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        createDTO.setUserId(userId);

        CoupleMailResponse.DetailResultDTO response =
                coupleMailService.createMail(createDTO);

        notificationService.sendCoupleMailNotification(createDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess(response));
    }

    /**
     * id를 통해 우편을 확인하는 API
     * @param accessToken
     * @param mailId
     * @return
     */
    @GetMapping("/{mailId}")
    public ResponseEntity<?> getMail(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable(name = "mailId") Integer mailId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);

        CoupleMailResponse.DetailResultDTO response =
                coupleMailService.getMail(userId, mailId);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /**
     * 좋아요를 표시하는 API
     * @param accessToken
     * @param mailId
     * @return
     */
    @PostMapping("/{mailId}/like")
    public ResponseEntity<?> toggleLike(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable(name = "mailId") Integer mailId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);

        coupleMailService.toggleLike(userId, mailId);

        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}