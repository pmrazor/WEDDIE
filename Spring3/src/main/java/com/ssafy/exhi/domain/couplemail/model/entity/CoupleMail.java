package com.ssafy.exhi.domain.couplemail.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.user.model.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CoupleMail 엔티티 요구사항: - 커플간 주고받는 편지 시스템으로 CR만 가능 (수정/삭제 불가) - 이미지 첨부파일 1개만 허용 (5MB 제한) - 읽음/안읽음 상태 관리 - 좋아요 기능 포함 -
 * MinioService를 통한 파일 저장
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoupleMail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_mail_id")
    private Integer id;

    // 편지 기본 정보
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    // 발신자/수신자 정보 (커플 내에서만 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    // 커플 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    // 상태 관리
    @Setter
    @Column(nullable = false)
    private boolean isRead;

    // 좋아요 상태
    @Setter
    @Column(nullable = false)
    private boolean isLiked;

    // 첨부파일 관리
    @Column(length = 255)
    private String attachmentName;  // Minio에 저장된 파일명

    @Setter
    @Transient
    private String attachmentUrl;  // 실제 파일 접근 URL (일시적 사용)

    // 템플릿 정보 (1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private MailTemplate template;
}
