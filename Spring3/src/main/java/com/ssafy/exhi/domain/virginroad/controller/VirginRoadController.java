package com.ssafy.exhi.domain.virginroad.controller;

import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.service.VirginRoadService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/virginRoad")
public class VirginRoadController {
    private final JWTUtil jwtUtil;
    private final VirginRoadService virginRoadService;

    @PostMapping
    public ResponseEntity<?> createVirginRoad(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody VirginRoadRequest.CreateDTO createDTO
            ) {

        Integer userId = jwtUtil.getUserId(accessToken);
        createDTO.setUserId(userId);

        VirginRoadResponse.SimpleResultDTO response = virginRoadService.createVirginRoad(createDTO);
        log.info("createPlan userId: {}", userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
