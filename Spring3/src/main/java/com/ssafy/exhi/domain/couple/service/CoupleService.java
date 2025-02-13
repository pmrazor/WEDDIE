package com.ssafy.exhi.domain.couple.service;

import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;

public interface CoupleService {
    CoupleResponse.SimpleResultDTO createCouple(CoupleRequest.CreateDTO coupleDTO);

    CoupleResponse.DetailResultDTO getCouple(Integer userId);

    void deleteCouple(Integer userId);

    CoupleResponse.SimpleResultDTO createCouple(Integer userId, Integer requestId);

    CoupleResponse.DetailResultDTO updateCouple(Integer userId, CoupleRequest.UpdateDTO updateDTO);
}
