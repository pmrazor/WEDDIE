package com.ssafy.exhi.domain.notice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse;
import com.ssafy.exhi.domain.notice.model.dto.NoticeResponse.PageDTO;
import com.ssafy.exhi.domain.notice.model.entity.Notice;
import com.ssafy.exhi.domain.notice.model.entity.NoticeCategory;
import com.ssafy.exhi.domain.notice.repository.NoticeRepository;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SpringBootTest
class NoticeServiceImplTest {
    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAll();
        userRepository.deleteAll();
        User user = User.builder()
                .loginId("loginId")
                .name("name")
                .password("password")
                .build();
        User save = userRepository.save(user);
        for (int i = 0; i < 3; i++) {
            for (NoticeCategory c : NoticeCategory.values()) {
                Notice testNotices = NoticeTestFixture.createNotice(
                        c, i + "", save
                );
                noticeRepository.save(testNotices);
            }

        }

    }

    @Test
    @DisplayName("카테고리별 최신 게시글 1개씩 조회 테스트")
    void getNewestNoticesTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        PageDTO result = noticeService.getNewestNotices(pageable);

        // then
        assertNotNull(result);
        List<NoticeResponse.SimpleResultDTO> notices = result.getContents(); // PageDTO의 구조에 따라 수정 필요

        assertEquals(3, notices.size()); // 3개의 카테고리이므로 3개의 결과가 나와야 함

        // 각 카테고리별로 최신 게시글만 포함되어 있는지 확인
        assertTrue(notices.stream()
                .anyMatch(notice -> notice.getTitle().equals("2")));
        assertTrue(notices.stream()
                .anyMatch(notice -> notice.getTitle().equals("2")));
        assertTrue(notices.stream()
                .anyMatch(notice -> notice.getTitle().equals("2")));

        // 오래된 게시글들은 포함되지 않았는지 확인
        assertFalse(notices.stream()
                .anyMatch(notice -> notice.getTitle().equals("0")));
        assertFalse(notices.stream()
                .anyMatch(notice -> notice.getTitle().equals("0")));
        assertFalse(notices.stream()
                .anyMatch(notice -> notice.getTitle().equals("0")));
    }
}