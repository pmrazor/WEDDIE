package com.ssafy.exhi.domain.tip.service;

import com.ssafy.exhi.domain.tip.model.dto.TipRequest;
import com.ssafy.exhi.domain.tip.model.dto.TipResponse;

import java.util.List;

public interface TipService {
    List<TipResponse.DetailResultDTO> getTips(Integer planId, Integer userId);

    TipResponse.DetailResultDTO createTip(TipRequest.CreateDTO createDTO);
}
