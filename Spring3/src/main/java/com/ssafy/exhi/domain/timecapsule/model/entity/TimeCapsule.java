package com.ssafy.exhi.domain.timecapsule.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.plan.model.entity.PlanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

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

    @Size(max = 255, message = "다퉜던 내용은 최대 100자까지 입력 가능합니다.")
    @Column(name="bad_content")
    private String badContent;

    @Column(name="bad_image")
    private String badImage;

    @Enumerated(EnumType.STRING)
    @Column(name="time_capsule_status")
    private PlanStatus timeCapsuleStatus;
}
