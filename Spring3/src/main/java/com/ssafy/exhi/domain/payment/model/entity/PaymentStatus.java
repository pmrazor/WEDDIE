package com.ssafy.exhi.domain.payment.model.entity;

public enum PaymentStatus {
    READY,              // 결제 생성 후 초기 상태
    IN_PROGRESS,        // 결제 인증 완료 상태
    WAITING_FOR_DEPOSIT,// 가상계좌 입금 대기 상태
    DONE,               // 결제 완료 상태
    CANCELED,           // 결제 취소 상태
    PARTIAL_CANCELED,   // 부분 취소 상태
    ABORTED,            // 결제 승인 실패 상태
    EXPIRED;            // 결제 유효 시간 초과 상태
}
