package com.ssafy.exhi.domain.couple.service;

import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;

public interface CoupleService {
    CoupleResponse.SimpleResultDTO createCouple(CoupleRequest.CreateDTO coupleDTO);
}
