package com.ssafy.exhi.domain.couplemail.service;

import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailResponse;

public interface CoupleMailService {
    CoupleMailResponse.DetailResultDTO createMail(CoupleMailRequest.CreateDTO createDTO);

    CoupleMailResponse.DetailResultDTO getMail(Integer userId, Integer mailId);

    void toggleLike(Integer userId, Integer mailId);

}
