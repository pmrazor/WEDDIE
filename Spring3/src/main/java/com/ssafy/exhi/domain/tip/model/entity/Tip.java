package com.ssafy.exhi.domain.tip.model.entity;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.plan.model.entity.Plan;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tip_id")
    private Integer id;

    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "tip_content", length = 2000)
    private String tipContent;

    @OneToOne(mappedBy = "tip", fetch = FetchType.LAZY)
    private Plan plan;

    @OneToOne
    @JoinColumn(name = "user_detail_id", referencedColumnName = "user_detail_id")
    private UserDetail userDetail;  // UserDetail 엔티티와의 연관 추가

    // gender 필드 추가 (UserDetail을 통해 Gender 접근)
    public Gender getGender() {
        return userDetail != null ? userDetail.getGender() : null;
    }
}