package com.ssafy.exhi.domain.payment.controller;

import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Value("${toss.api.url}")
    private String tossApiUrl;

    @Value("${toss.secret.key}")
    private String secretKey;

    @Value("${toss.success.url}")
    private String successUrl;

    @Value("${toss.fail.url}")
    private String failUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/request")
    public ResponseEntity<?> requestPayment(@RequestBody PaymentRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.set("Content-Type", "application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("amount", request.getAmount());
            body.put("orderId", request.getOrderId());
            body.put("orderName", request.getOrderName());
            body.put("successUrl", successUrl);
            body.put("failUrl", failUrl);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossApiUrl + "/v1/payments/confirm",
                    new org.springframework.http.HttpEntity<>(body, headers),
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("결제 요청 실패: " + e.getMessage());
        }
    }


    @GetMapping("/fail")
    public ResponseEntity<?> paymentFailed(@RequestParam Map<String, String> params) {
        return ResponseEntity.badRequest().body(params);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(@RequestParam String paymentKey, @RequestParam String cancelReason) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.set("Content-Type", "application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("cancelReason", cancelReason);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossApiUrl + "/v1/payments/" + paymentKey + "/cancel",
                    new org.springframework.http.HttpEntity<>(body, headers),
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("결제 취소 실패: " + e.getMessage());
        }
    }
}
