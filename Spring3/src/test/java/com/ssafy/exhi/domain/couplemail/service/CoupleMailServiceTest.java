package com.ssafy.exhi.domain.couplemail.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailResponse;
import com.ssafy.exhi.domain.couplemail.model.entity.CoupleMail;
import com.ssafy.exhi.domain.couplemail.model.entity.MailTemplate;
import com.ssafy.exhi.domain.couplemail.repository.CoupleMailRepository;
import com.ssafy.exhi.domain.couplemail.repository.MailTemplateRepository;
import com.ssafy.exhi.domain.notice.service.MinioImageService;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

/**
 * CoupleMailService의 단위 테스트 - Mockito를 사용하여 의존성을 모킹 - 각 서비스 메서드의 성공 케이스만 테스트
 */
@ExtendWith(MockitoExtension.class)
class CoupleMailServiceTest {

    @Mock
    private CoupleMailRepository coupleMailRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CoupleRepository coupleRepository;
    @Mock
    private MailTemplateRepository mailTemplateRepository;
    @Mock
    private MinioImageService minioImageService;

    @InjectMocks
    private CoupleMailServiceImpl coupleMailService;

    @Test
    @DisplayName("메일 생성 성공 테스트")
    void createMailSuccess() {
        // Given
        CoupleMailRequest.CreateDTO createDTO = createSampleDTO();
        User sender = createSampleUser(1);
        User receiver = createSampleUser(2);
        Couple couple = createSampleCouple(sender, receiver);
        MailTemplate template = createSampleTemplate();

        // Mock 설정
        when(userRepository.findUserById(any())).thenReturn(Optional.of(sender));
        when(coupleRepository.findCoupleByUserId(any())).thenReturn(Optional.of(couple));
        when(mailTemplateRepository.findById(any())).thenReturn(Optional.of(template));
        when(minioImageService.uploadImage(any())).thenReturn("sample.jpg");
        when(coupleMailRepository.save(any())).thenReturn(
                createSampleMail(sender, receiver, couple, template, "sample.jpg"));

        // When
        CoupleMailResponse.DetailResultDTO result = coupleMailService.createMail(createDTO);

        // Then
        assertNotNull(result);
        assertEquals("테스트 메일", result.getTitle());
        assertEquals("sample.jpg", result.getAttachmentFileName());
        verify(coupleMailRepository).save(any());
    }

    @Test
    @DisplayName("메일 조회 성공 테스트")
    void getMailSuccess() {
        // Given
        Integer userId = 1;
        Integer mailId = 1;
        User sender = createSampleUser(2);
        User receiver = createSampleUser(1);
        Couple couple = createSampleCouple(sender, receiver);
        MailTemplate template = createSampleTemplate();
        CoupleMail mail = createSampleMail(sender, receiver, couple, template, "sample.jpg");

        when(coupleMailRepository.findMailWithAuth(mailId, userId))
                .thenReturn(Optional.of(mail));
        when(minioImageService.getImageUrl(any())).thenReturn("http://sample.url");

        // When
        CoupleMailResponse.DetailResultDTO result = coupleMailService.getMail(userId, mailId);

        // Then
        assertNotNull(result);
        assertEquals(mail.getTitle(), result.getTitle());
        assertTrue(mail.isRead());  // 읽음 처리 확인
    }

    @Test
    @DisplayName("좋아요 토글 성공 테스트")
    void toggleLikeSuccess() {
        // Given
        Integer userId = 1;
        Integer mailId = 1;
        CoupleMail mail = createSampleMail();

        when(coupleMailRepository.findMailWithAuth(mailId, userId))
                .thenReturn(Optional.of(mail));

        // When
        coupleMailService.toggleLike(userId, mailId);

        // Then
        assertTrue(mail.isLiked());  // 좋아요 상태가 토글되었는지 확인
    }

    // 테스트 헬퍼 메서드들
    private CoupleMailRequest.CreateDTO createSampleDTO() {
        return CoupleMailRequest.CreateDTO.builder()
                .userId(1)
                .title("테스트 메일")
                .content("테스트 내용")
                .attachment(new MockMultipartFile("asd.jpg", "asd.jpg", "image/jpg", "asda".getBytes()))
                .templateId(1)
                .build();
    }

    private User createSampleUser(Integer id) {
        return User.builder()
                .id(id)
                .name("테스트 유저 " + id)
                .build();
    }

    private Couple createSampleCouple(User male, User female) {
        return Couple.builder()
                .id(1)
                .male(male)
                .female(female)
                .build();
    }

    private MailTemplate createSampleTemplate() {
        return MailTemplate.builder()
                .id(1)
                .title("템플릿 제목")
                .content("템플릿 내용")
                .build();
    }

    private CoupleMail createSampleMail() {
        return CoupleMail.builder()
                .id(1)
                .title("테스트 메일")
                .content("테스트 내용")
                .isRead(false)
                .isLiked(false)
                .build();
    }

    private CoupleMail createSampleMail(User sender, User receiver, Couple couple, MailTemplate template,
                                        String image) {

        return CoupleMail.builder()
                .id(1)
                .title("테스트 메일")
                .content("테스트 내용")
                .sender(sender)
                .receiver(receiver)
                .couple(couple)
                .template(template)
                .attachmentName(image)
                .isRead(false)
                .isLiked(false)
                .build();
    }
}