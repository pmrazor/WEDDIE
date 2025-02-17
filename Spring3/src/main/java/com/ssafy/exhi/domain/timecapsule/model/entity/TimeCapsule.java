package com.ssafy.exhi.domain.timecapsule.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TimeCapsule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_capsule_id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "couple_id")
    private Integer coupleId;

    @Size(max = 255, message = "좋았던 점은 최대 100자까지 입력 가능합니다.")
    @Column(name="good_content")
    private String goodContent;

    @Column(name="good_image")
    private String goodImage;

    @Setter
    @Transient
    private String goodImagePath;

    @Size(max = 255, message = "다퉜던 내용은 최대 100자까지 입력 가능합니다.")
    @Column(name="bad_content")
    private String badContent;

    @Column(name="bad_image")
    private String badImage;

    @Setter
    @Transient
    private String badImagePath;

    @Enumerated(EnumType.STRING)
    @Column(name="time_capsule_status")
    private PlanStatus timeCapsuleStatus;

    @Description("Good = 0, Bad = 1")
    @Column(name="plan_grade")
    private Integer planGrade;
}
