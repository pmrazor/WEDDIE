package com.ssafy.exhi.domain.reservation.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.reservation.model.dto.ReservationRequest;
import com.ssafy.exhi.domain.reservation.model.entity.Reservation;
import com.ssafy.exhi.domain.reservation.service.ReservationService;
import com.ssafy.exhi.util.JWTUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final JWTUtil jwtUtil;
    private final ReservationService reservationService;

    // 전체 예약 조회를 한다
    @GetMapping
    private ResponseEntity<?> getReservationAll(
            @RequestHeader(name = "Authorization") String accessToken
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        List<Reservation> response = reservationService.getAllReservations(userId);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    // 예약을 상세 조회한다
    @PostMapping("/{reservationId}") // 이거 GET 말고 POST 맞나요?! 맞겠죠 맞으니까 했겠지...
    private ResponseEntity<?> getReservation(
            @RequestHeader(name = "Authorization") String accessToken,
            @PathVariable("reservationId") Integer reservationId
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        Reservation response = reservationService.getReservation(userId, reservationId);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    // 예약을 한다
    @PostMapping("/{shopId}/{itemId}")
    private ResponseEntity<?> createReservation(
            @RequestHeader(name = "Authorization") String accessToken,
            @RequestBody ReservationRequest.CreateDTO createDTO
    ) {

        Integer userId = jwtUtil.getUserId(accessToken);
        Reservation response = reservationService.save(userId, createDTO);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }
}
