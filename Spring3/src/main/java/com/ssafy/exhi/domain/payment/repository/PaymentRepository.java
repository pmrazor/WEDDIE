package com.ssafy.exhi.domain.payment.repository;

import com.ssafy.exhi.domain.payment.model.entity.Payment;
import com.ssafy.exhi.domain.order.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);

    // ✅ Order 객체를 기반으로 Payment 조회하는 메서드 추가
    Optional<Payment> findByOrder(Order order);
}

