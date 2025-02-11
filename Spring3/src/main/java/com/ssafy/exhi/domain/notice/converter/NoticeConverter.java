package com.ssafy.exhi.domain.notice.converter;

import com.ssafy.exhi.domain.notice.model.dto.NoticeRequest;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse;
import com.ssafy.exhi.domain.notice.model.entity.Notice;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.dto.UserResponse.SimpleResultDTO;
import com.ssafy.exhi.domain.user.model.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;

public class NoticeConverter {

    public static Notice toEntity(User user, NoticeRequest.CreateDTO dto, String imageName) {

        return Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .noticeCategory(dto.getCategory())
                .imageName(imageName)
                .user(user)
                .build();
    }

    public static NoticeResponse.SimpleResultDTO toSimpleDTO(Notice entity) {

        SimpleResultDTO user = UserConverter.toSimpleResultDTO(entity.getUser());

        return NoticeResponse.SimpleResultDTO.builder()
                .id(entity.getId())
                .user(user)
                .title(entity.getTitle())
                .content(entity.getContent())
                .category(entity.getNoticeCategory())
                .build();
    }

    public static NoticeResponse.DetailResultDTO toDetailDTO(Notice entity, String imageUrl) {

        SimpleResultDTO user = UserConverter.toSimpleResultDTO(entity.getUser());

        return NoticeResponse.DetailResultDTO.builder()
                .id(entity.getId())
                .user(user)
                .title(entity.getTitle())
                .content(entity.getContent())
                .category(entity.getNoticeCategory())
                .imageUrl(imageUrl)
                .build();
    }

    public static NoticeResponse.PageDTO toPageDTO(Page<Notice> page) {
        List<NoticeResponse.SimpleResultDTO> dtoList = page.getContent().stream().map(
                NoticeConverter::toSimpleDTO
        ).toList();

        return NoticeResponse.PageDTO.builder()
                .contents(dtoList)
                .listSize(dtoList.size())
                .isFirstPage(page.isFirst())
                .isLastPage(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }
}

