package com.ssafy.exhi.domain.payment.service;

import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    PaymentResponse.PaymentDTO getPayment(Integer paymentId);
}
