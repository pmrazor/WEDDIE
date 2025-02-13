package com.ssafy.exhi.domain.payment.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import com.ssafy.exhi.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ✅ 결제 요청 API (Toss 결제창 띄우기)
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest.CreateDTO createRequest) {
        return ResponseEntity.ok(paymentService.createPayment(createRequest));
    }

    // ✅ 결제 승인 API (Toss에서 successUrl로 요청 보낼 때 실행됨)
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentRequest.ConfirmDTO confirmRequest) {
        PaymentResponse.ConfirmResultDTO response = paymentService.confirmPayment(confirmRequest);
        return ResponseEntity.ok(response);
    }
}
