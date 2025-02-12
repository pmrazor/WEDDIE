package com.ssafy.exhi.domain.tip.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.tip.model.dto.TipRequest;
import com.ssafy.exhi.domain.tip.model.dto.TipResponse;
import com.ssafy.exhi.domain.tip.repository.TipRepository;
import com.ssafy.exhi.domain.tip.service.TipService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TipController {

    private final JWTUtil jwtUtil;
    private final TipService tipService;
    private final TipRepository tipRepository;

    /**
     * @param planId
     * @return
     */
    @GetMapping("/plan/{planId}/tip")
    public ResponseEntity<?> getTips(
        @RequestHeader(name = "Authorization") String accessToken,
        @PathVariable("planId") Integer planId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        List<TipResponse.DetailResultDTO> response = tipService.getTips(planId, userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /**
     * 관리자용
     *
     * @return
     */
    @PostMapping("/tip")
    public ResponseEntity<?> createTip(
        @RequestHeader(name = "Authorization") String accessToken,
        @RequestBody @Valid TipRequest.CreateDTO createDTO
    ) {
        // TODO 관리자 인가 로직 추가 필요
        TipResponse.DetailResultDTO response = tipService.createTip(createDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
