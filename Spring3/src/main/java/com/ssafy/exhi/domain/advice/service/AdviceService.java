package com.ssafy.exhi.domain.advice.service;

import com.ssafy.exhi.domain.ServiceType;

public interface AdviceService {
    String getAdvice(Integer loginId, boolean messageType);
}
