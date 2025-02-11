package com.ssafy.exhi.domain.payment.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String orderId;     // 주문 ID (고유 값, Toss 결제 요청 시 사용)
    private String orderName;   // 주문 이름
    private int amount;         // 결제 금액
    private String successUrl;  // 결제 성공 후 이동할 URL
    private String failUrl;     // 결제 실패 후 이동할 URL
    private String customerEmail;  // 고객 이메일
    private String customerName;   // 고객 이름
    private String customerMobilePhone;  // 고객 휴대폰 번호
}

