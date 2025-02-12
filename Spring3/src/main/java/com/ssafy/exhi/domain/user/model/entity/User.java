package com.ssafy.exhi.domain.user.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.oauth.AuthProvider;
import com.ssafy.exhi.domain.recommendation.model.entity.UserCluster;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "login_id", unique = true)
    private String loginId;

    @Column(name = "password")
    private String password;

    @Column(name = "token")
    @Setter
    private String token;

    @Transient
    private Integer coupleId;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserDetail userDetail;

    @Column(name = "profile_img")
    private String profileImg;

    /* OAuth 필드 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    /* OAuth 필드 끝 */

    @PrePersist
    public void prePersist() {
        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL;
        }
    }

    public User update(UserRequest.UpdateDTO updateDTO) {
        this.name = updateDTO.getName();
        this.password = updateDTO.getNewPassword();
        this.profileImg = updateDTO.getProfileImg();
        return this;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
        userDetail.setUser(this); // 양방향 관계 설정
    }


    /* recommendation 필드 */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserCluster userCluster;

    // 양방향 편의 메서드
    public void setUserCluster(UserCluster userCluster) {
        this.userCluster = userCluster;
        userCluster.setUser(this);
    }
    /* recommendation 필드 끝 */

    // 유저 기본 정보 수정 위한 저장 로직
    public void updateInfo(UserRequest.UpdateInfoDTO dto) {
        this.name = dto.getName();
        this.profileImg = dto.getProfileImg();
    }

    // 유저 기존 정보 수정 시 새로운 요청 정보를 비교하는 메서드
    public boolean isSameInfo(UserRequest.UpdateInfoDTO dto) {
        return Objects.equals(this.name, dto.getName()) &&
                Objects.equals(this.profileImg, dto.getProfileImg());
    }

}
