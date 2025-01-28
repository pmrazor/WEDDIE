package com.ssafy.exhi.domain.couple.controller;

import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.service.CoupleService;
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

    /**
     * 커플
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


}
