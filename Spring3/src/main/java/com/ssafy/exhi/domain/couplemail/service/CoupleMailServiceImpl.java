package com.ssafy.exhi.domain.couplemail.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.couplemail.converter.CoupleMailConverter;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest.CreateDTO;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailResponse;
import com.ssafy.exhi.domain.couplemail.model.entity.CoupleMail;
import com.ssafy.exhi.domain.couplemail.model.entity.MailTemplate;
import com.ssafy.exhi.domain.couplemail.repository.CoupleMailRepository;
import com.ssafy.exhi.domain.couplemail.repository.MailTemplateRepository;
import com.ssafy.exhi.domain.notice.service.MinioImageService;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CoupleMailServiceImpl implements CoupleMailService {

    private final CoupleMailRepository coupleMailRepository;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final MailTemplateRepository mailTemplateRepository;
    private final MinioImageService minioImageService;

    @Override
    public CoupleMailResponse.DetailResultDTO createMail(CoupleMailRequest.CreateDTO createDTO) {
        // 1. 발신자 정보 조회 및 검증
        User sender = findUserById(createDTO.getUserId());
        Couple couple = findCoupleByUserId(createDTO.getUserId());

        // 2. 수신자 결정 (커플 중 발신자가 아닌 사람)
        User receiver = sender.equals(couple.getMale()) ?
                couple.getFemale() : couple.getMale();

        // 3. 템플릿 조회
        MailTemplate template = findTemplateById(createDTO.getTemplateId());

        // 4. 첨부파일 처리
        String attachmentName = fileUpload(createDTO);

        // 5. 메일 생성 및 저장
        CoupleMail mail = CoupleMailConverter.toEntity(
                createDTO, sender, receiver, couple, template, attachmentName
        );
        CoupleMail saved = coupleMailRepository.save(mail);
        createDTO.setMailId(saved.getId());

        return CoupleMailConverter.toDetailDTO(saved);
    }

    private String fileUpload(CreateDTO createDTO) {
        String attachmentName = null;
        if (createDTO.getAttachment() != null) {
            validateAttachment(createDTO.getAttachment());
            attachmentName = minioImageService.uploadImage(createDTO.getAttachment());
        }
        return attachmentName;
    }

    @Override
    public CoupleMailResponse.DetailResultDTO getMail(Integer userId, Integer mailId) {
        CoupleMail mail = findMailWithAuth(mailId, userId);

        // 수신자가 조회하는 경우 읽음 처리
        if (mail.getReceiver().getId().equals(userId) && !mail.isRead()) {
            mail.setRead(true);
        }

        // 첨부파일 URL 설정
        mail.setAttachmentUrl(minioImageService.getImageUrl(mail.getAttachmentName()));

        return CoupleMailConverter.toDetailDTO(mail);
    }

    @Override
    public void toggleLike(Integer userId, Integer mailId) {
        CoupleMail mail = findMailWithAuth(mailId, userId);
        mail.setLiked(!mail.isLiked());
    }

    // 유효성 검사 메서드들
    private void validateAttachment(MultipartFile file) {

        // 이미지 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ExceptionHandler(ErrorStatus.INVALID_FILE_TYPE);
        }
    }

    /**
     * 메일 조회 + 권한 검증을 동시에 수행하는 헬퍼 메서드 - 메일이 존재하는지 확인 - 요청한 사용자가 해당 메일의 발신자나 수신자인지 확인
     */
    private CoupleMail findMailWithAuth(Integer mailId, Integer userId) {
        return coupleMailRepository.findMailWithAuth(mailId, userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.MAIL_NOT_FOUND));
    }

    /**
     * 템플릿 조회 헬퍼 메서드 - 존재하는 템플릿인지 확인 - 추후 템플릿의 유효성이나 사용 가능 여부 등을 검증하는 로직 추가 가능
     */
    private MailTemplate findTemplateById(Integer templateId) {
        if (templateId == null) {
            return null;  // 템플릿 선택은 선택사항일 수 있음
        }

        return mailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.TEMPLATE_NOT_FOUND));
    }

    /**
     * 커플 정보 조회 헬퍼 메서드 - 사용자 ID로 해당 사용자의 커플 정보 조회 - 커플 관계가 존재하는지 확인
     */
    private Couple findCoupleByUserId(Integer userId) {
        return coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
    }

    /**
     * 사용자 정보 조회 헬퍼 메서드 - 사용자 존재 여부 확인 - 추후 사용자의 상태나 권한 등을 검증하는 로직 추가 가능
     */
    private User findUserById(Integer userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }
}