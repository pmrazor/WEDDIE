package com.ssafy.exhi.domain.notice.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.notice.converter.NoticeConverter;
import com.ssafy.exhi.domain.notice.model.dto.NoticeRequest.CreateDTO;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse.DetailResultDTO;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse.PageDTO;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse.SimpleResultDTO;
import com.ssafy.exhi.domain.notice.model.entity.Notice;
import com.ssafy.exhi.domain.notice.repository.NoticeRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final MinioImageService minioImageService;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Override
    public SimpleResultDTO createNotice(CreateDTO dto) {
        User user = findUserByUserId(dto.getUserId());
        String imageName = minioImageService.uploadImage(dto.getImage());

        Notice entity = NoticeConverter.toEntity(user, dto, imageName);
        Notice saved = noticeRepository.save(entity);

        return NoticeConverter.toSimpleDTO(saved);
    }

    @Override
    public PageDTO getNotices(Pageable pageable) {
        Page<Notice> page = noticeRepository.findAll(pageable);
        page.getContent().forEach(notice ->
                notice.setImageUrl(minioImageService.getImageUrl(notice.getImageName()))
        );

        return NoticeConverter.toPageDTO(page);
    }

    @Override
    public PageDTO getNewestNotices(Pageable pageable) {
        Page<Notice> page = noticeRepository.findNewestNotices(pageable);
        page.getContent().forEach(notice ->
                notice.setImageUrl(minioImageService.getImageUrl(notice.getImageName()))
        );

        return NoticeConverter.toPageDTO(page);
    }

    @Override
    public DetailResultDTO getNotice(Integer noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.NOTICE_NOT_FOUND)
        );
        String imageUrl = minioImageService.getImageUrl(notice.getImageName());

        return NoticeConverter.toDetailDTO(notice, imageUrl);

    }

    private User findUserByUserId(Integer userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
        );
    }
}
