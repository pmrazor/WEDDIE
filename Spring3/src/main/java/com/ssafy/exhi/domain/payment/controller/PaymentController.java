package com.ssafy.exhi.domain.payment.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import com.ssafy.exhi.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ✅ 결제 요청 API (Toss 결제창 띄우기)
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest.CreateDTO request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    // ✅ 결제 승인 API (Toss에서 successUrl로 요청 보낼 때 실행됨)
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentRequest.ConfirmDTO confirmRequest) {
        PaymentResponse.CreateResultDTO response = paymentService.confirmPayment(confirmRequest);
        return ResponseEntity.ok(response);
    }
}
