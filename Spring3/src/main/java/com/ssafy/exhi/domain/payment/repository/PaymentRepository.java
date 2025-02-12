package com.ssafy.exhi.domain.payment.repository;

import com.ssafy.exhi.domain.payment.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 기존 CRUD 메소드들 외에 paymentKey로 Payment를 조회할 수 있는 메소드 추가
    Optional<Payment> findByPaymentKey(String paymentKey);
    Optional<Payment> findByOrderId(String orderId);  // 주문 ID로 조회
}
