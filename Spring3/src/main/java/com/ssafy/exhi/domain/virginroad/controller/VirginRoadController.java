package com.ssafy.exhi.domain.virginroad.controller;

import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;
import com.ssafy.exhi.domain.virginroad.service.VirginRoadService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
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
        log.info("createVirginRoad userId: {}", userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 버진로드 최초 화면에 사용되는 API
     * 결혼 과정의 상태를 전체적으로 보여준다.
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getVirginRoad(
            @RequestHeader("Authorization") String accessToken
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("getVirginRoad userId: {}", userId);

        VirginRoadResponse.SimpleResultDTO response = virginRoadService.getVirginRoad(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 버진로드를 수정한다.
     * @param accessToken
     * @return
     */
    @PutMapping
    public ResponseEntity<?> updateVirginRoad(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid VirginRoadRequest.UpdateDTO updateDTO
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        updateDTO.setUserId(userId);
        log.info("updateVirginRoad userId: {}", userId);

        VirginRoadResponse.SimpleResultDTO response = virginRoadService.updateVirginRoad(updateDTO);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping
    public ResponseEntity<?> deleteVirginRoad(
            @RequestHeader("Authorization") String accessToken
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("deleteVirginRoad userId: {}", userId);

        virginRoadService.deleteVirginRoad(userId);

        return ResponseEntity.noContent().build();
    }
}
