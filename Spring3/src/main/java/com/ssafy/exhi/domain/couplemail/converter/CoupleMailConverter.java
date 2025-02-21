package com.ssafy.exhi.domain.couplemail.converter;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailRequest;
import com.ssafy.exhi.domain.couplemail.model.dto.CoupleMailResponse;
import com.ssafy.exhi.domain.couplemail.model.entity.CoupleMail;
import com.ssafy.exhi.domain.couplemail.model.entity.MailTemplate;
import com.ssafy.exhi.domain.user.model.entity.User;

/**
 * CoupleMail 엔티티와 DTO 간의 변환을 담당하는 컨버터
 */
public class CoupleMailConverter {

    public static CoupleMail toEntity(CoupleMailRequest.CreateDTO dto,
                                      User sender,
                                      User receiver,
                                      Couple couple,
                                      MailTemplate template,
                                      String attachmentName) {
        return CoupleMail.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .sender(sender)
                .receiver(receiver)
                .couple(couple)
                .template(template)
                .attachmentName(attachmentName)
                .isRead(false)
                .isLiked(false)
                .build();
    }

    public static CoupleMailResponse.DetailResultDTO toDetailDTO(CoupleMail mail) {
        return CoupleMailResponse.DetailResultDTO.builder()
                .mailId(mail.getId())
                .title(mail.getTitle())
                .content(mail.getContent())
                .senderName(mail.getSender().getName())
                .receiverName(mail.getReceiver().getName())
                .isRead(mail.isRead())
                .isLiked(mail.isLiked())
                .attachmentFileName(mail.getAttachmentName())
                .createdAt(mail.getCreatedAt())
                .template(toTemplateDTO(mail.getTemplate()))
                .build();
    }

    private static CoupleMailResponse.MailTemplateDTO toTemplateDTO(MailTemplate template) {
        if (template == null) {
            return null;
        }
        return CoupleMailResponse.MailTemplateDTO.builder()
                .templateId(template.getId())
                .title(template.getTitle())
                .content(template.getContent())
                .description(template.getDescription())
                .serviceType(template.getServiceType())
                .build();
    }
}