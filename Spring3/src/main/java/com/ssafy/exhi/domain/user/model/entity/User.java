package com.ssafy.exhi.domain.user.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.oauth.AuthProvider;
import com.ssafy.exhi.domain.recommendation.model.entity.UserCluster;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@Entity
@Schema(name = "User", description = "유저 엔티티")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

//    @Column(name = "gender")
//    private Gender gender;

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

    @PrePersist
    public void prePersist() {
        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL;
        }
    }

    /* OAuth 필드 끝 */

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
    @OneToOne(mappedBy = "user", orphanRemoval = true)
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
//        this.gender = dto.getGender();
        this.profileImg = dto.getProfileImg();
    }

}
