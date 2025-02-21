package com.ssafy.exhi.domain.notice.service;

import com.ssafy.exhi.domain.notice.model.entity.Notice;
import com.ssafy.exhi.domain.notice.model.entity.NoticeCategory;
import com.ssafy.exhi.domain.user.model.entity.User;

public class NoticeTestFixture {
    public static Notice createNotice(NoticeCategory category, String title, User user) {
        return Notice.builder()
                .noticeCategory(category)
                .title(title)
                .content("Test content for " + title)
                .imageName("http://example.com/image.jpg")
                .user(user)
                .build();
    }

}