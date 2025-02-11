package com.ssafy.exhi.domain.payment.service;

import com.ssafy.exhi.domain.payment.model.dto.PaymentRequest;
import com.ssafy.exhi.domain.payment.model.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

public interface PaymentService {
    PaymentResponse.CreateResultDTO createPayment(PaymentRequest.CreateDTO paymentRequest);
    PaymentResponse.CreateResultDTO confirmPayment(PaymentRequest.ConfirmDTO confirmRequest);
}
