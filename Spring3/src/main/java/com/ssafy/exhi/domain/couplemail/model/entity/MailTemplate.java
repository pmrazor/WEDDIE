package com.ssafy.exhi.domain.couplemail.model.entity;

import com.ssafy.exhi.domain.ServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MailTemplate 엔티티 요구사항: - 결혼 준비 과정에서의 감정 전달을 위한 템플릿 - 텍스트 기반의 템플릿 제공 - 각 메일과 1:1 관계
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_template_id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;  // 템플릿 제목

    @Column(name = "content", nullable = false, length = 2000)
    private String content;  // 템플릿 내용

    @Column(name = "description", nullable = false)
    private String description;  // 템플릿 설명 (언제 사용하면 좋은지 등)

    // 해당 템플릿이 관련된 결혼 준비 단계
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    // 템플릿과 연결된 메일
    @OneToOne(mappedBy = "template")
    private CoupleMail coupleMail;
}