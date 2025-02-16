package com.ssafy.exhi.domain.couplemail.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailResponse;
import com.ssafy.exhi.domain.couplemail.model.entity.CoupleMail;
import com.ssafy.exhi.domain.couplemail.model.entity.MailTemplate;
import com.ssafy.exhi.domain.couplemail.repository.CoupleMailRepository;
import com.ssafy.exhi.domain.couplemail.repository.MailTemplateRepository;
import com.ssafy.exhi.domain.notice.service.MinioImageService;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * CoupleMailService의 통합 테스트 - 실제 스프링 컨텍스트를 로드하여 테스트 - 실제 DB를 사용하여 테스트 (테스트용 H2 데이터베이스 권장)
 */
@SpringBootTest
@Transactional
class CoupleMailServiceIntegrationTest {

    @Autowired
    private CoupleMailService coupleMailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private CoupleRepository coupleRepository;

    @Autowired
    private MailTemplateRepository mailTemplateRepository;

    @MockBean  // MinioService는 외부 의존성이므로 모킹
    private MinioImageService minioImageService;

    private User sender;
    private User receiver;
    private MailTemplate template;
    private CoupleMail coupleMail;
    @Autowired
    private CoupleMailRepository coupleMailRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        sender = userRepository.save(createSampleUser(1));
        receiver = userRepository.save(createSampleUser(2));
        userDetailRepository.save(createSampleUserDetail(sender, Gender.MALE));
        userDetailRepository.save(createSampleUserDetail(receiver, Gender.FEMALE));
        coupleRepository.save(createSampleCouple(sender, receiver));
        template = mailTemplateRepository.save(createSampleTemplate(coupleMail));

        // MinioService 모킹
        when(minioImageService.uploadImage(any())).thenReturn("test.jpg");
        when(minioImageService.getImageUrl(any())).thenReturn("http://test.url");
    }

    @Test
    @DisplayName("메일 생성 통합 테스트")
    void createMailIntegrationTest() {
        // Given
        CoupleMailRequest.CreateDTO createDTO = CoupleMailRequest.CreateDTO.builder()
                .userId(sender.getId())
                .title("통합 테스트 메일")
                .content("통합 테스트 내용")
                .templateId(template.getId())
                .build();

        // When
        CoupleMailResponse.DetailResultDTO result = coupleMailService.createMail(createDTO);

        // Then
        assertNotNull(result);
        assertEquals("통합 테스트 메일", result.getTitle());
        assertEquals(sender.getName(), result.getSenderName());
        assertEquals(receiver.getName(), result.getReceiverName());
    }

    @Test
    @DisplayName("메일 조회 통합 테스트")
    void getMailIntegrationTest() {
        // Given
        CoupleMailRequest.CreateDTO createDTO = CoupleMailRequest.CreateDTO.builder()
                .userId(sender.getId())
                .title("조회용 테스트 메일")
                .content("조회용 테스트 내용")
                .templateId(template.getId())
                .build();

        CoupleMailResponse.DetailResultDTO created = coupleMailService.createMail(createDTO);

        // When
        CoupleMailResponse.DetailResultDTO result =
                coupleMailService.getMail(receiver.getId(), created.getMailId());

        // Then
        assertNotNull(result);
        assertEquals("조회용 테스트 메일", result.getTitle());
        assertTrue(result.isRead());  // 수신자가 조회했으므로 읽음 처리되어야 함
    }

    private User createSampleUser(Integer id) {
        return User.builder()
                .name("테스트 유저 " + id)
                .build();
    }

    private UserDetail createSampleUserDetail(User user, Gender gender) {
        return UserDetail.builder()
                .user(user)
                .gender(gender)
                .build();
    }

    private Couple createSampleCouple(User male, User female) {
        return Couple.builder()
                .male(male)
                .female(female)
                .build();
    }

    private MailTemplate createSampleTemplate(CoupleMail coupleMail) {
        return MailTemplate.builder()
                .id(1)
                .title("템플릿 제목")
                .content("템플릿 내용")
                .description("템플릿 설명")
                .serviceType(ServiceType.DRESS_SHOP)
                .coupleMail(coupleMail)
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
}