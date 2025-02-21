package com.ssafy.exhi.domain.notice.service;

import com.ssafy.exhi.domain.notice.model.dto.NoticeRequest.CreateDTO;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse.DetailResultDTO;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse.PageDTO;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse.SimpleResultDTO;
import org.springframework.data.domain.Pageable;

public interface NoticeService {

    SimpleResultDTO createNotice(CreateDTO dto);

    PageDTO getNotices(Pageable pageable);

    PageDTO getNewestNotices(Pageable pageable);

    DetailResultDTO getNotice(Integer noticeId);
}
