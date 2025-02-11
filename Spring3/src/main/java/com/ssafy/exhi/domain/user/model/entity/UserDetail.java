package com.ssafy.exhi.domain.user.model.entity;

import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

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

  @Column(name = "gender")
  private Gender gender;

    @Column(name = "phone")
    private String phone;

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
    this.gender = dto.getGender();
    this.phone = dto.getPhone();
    this.mbti = dto.getMbti();
  }

  // 유저 상세 정보 생성 위한 저장 로직
  public void createDetail(UserRequest.CreateDetailDTO dto) {
    this.age = dto.getAge();
    this.gender = dto.getGender();
    this.phone = dto.getPhone();
    this.mbti = dto.getMbti();
  }

  // 유저 상세 정보 조회 위한 로직
  public void getDetail(UserRequest.getDetailDTO dto) {
    this.age = dto.getAge();
    this.gender = dto.getGender();
    this.phone = dto.getPhone();
    this.mbti = dto.getMbti();
  }

    // 유저 상세 정보 수정 시 새로운 요청 정보를 비교하는 메서드
    public boolean isSameDetail(UserRequest.UpdateDetailDTO dto) {
        return Objects.equals(this.age, dto.getAge()) &&
                Objects.equals(this.gender, dto.getGender()) &&
                Objects.equals(this.phone, dto.getPhone()) &&
                Objects.equals(this.mbti, dto.getMbti());
    }

}
