package com.ssafy.exhi.domain.payment.controller;

import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import com.ssafy.exhi.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ✅ 결제 요청 API (Toss 결제창 띄우기)
    @PostMapping("/request")
    public ResponseEntity<?> requestPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.requestPayment(request));
    }

    // ✅ 결제 승인 API (Toss에서 successUrl로 요청 보낼 때 실행됨)
    @GetMapping("/success")
    public ResponseEntity<?> confirmPayment(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount) {
        PaymentResponse response = paymentService.confirmPayment(paymentKey, orderId, amount);
        return ResponseEntity.ok(response);
    }
}
