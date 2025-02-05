package com.ssafy.exhi.domain.user.model.entity;

import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Builder
@Schema(name = "UserDetail", description = "유저 디테일 엔티티")
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail {
    @Id
    @Column(name = "user_detail_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "age")
    private Integer age;

    @Column(name = "phone")
    private String phone;

    @Column(name = "budget")
    private Integer budget;

    @Column(name = "mbti")
    private String mbti;

    // 양방향 관계 설정을 위한 메서드 추가
    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getUserDetail() != this) {
            user.setUserDetail(this);
        }
    }

    // 유저 상세 정보 수정 위한 저장 로직
    public void updateDetail(UserRequest.UpdateDetailDTO dto) {
        this.age = dto.getAge();
        this.phone = dto.getPhone();
        this.mbti = dto.getMbti();
    }

    // 유저 상세 정보 생성 위한 저장 로직
    public void createDetail(UserRequest.CreateDetailDTO dto) {
        this.age = dto.getAge();
        this.phone = dto.getPhone();
        this.mbti = dto.getMbti();
    }

    public void getDetail(UserRequest.getDetailDTO dto) {
        this.age = dto.getAge();
        this.phone = dto.getPhone();
        this.mbti = dto.getMbti();
    } 

}
