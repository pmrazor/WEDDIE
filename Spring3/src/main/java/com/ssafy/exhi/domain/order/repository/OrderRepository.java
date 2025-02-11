package com.ssafy.exhi.domain.order.repository;

import com.ssafy.exhi.domain.order.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
