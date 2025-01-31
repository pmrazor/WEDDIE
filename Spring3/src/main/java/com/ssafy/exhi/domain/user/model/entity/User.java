package com.ssafy.exhi.domain.user.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.oauth.AuthProvider;
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
    private Integer userId;

    @Column(name = "department")
    private String department;

    @Column(name = "position")
    private String position;

    @Column(name = "name")
    private String name;

    @Column(name = "login_id", unique = true)
    private String loginId;

    @Column(name = "password")
    private String password;

    @Column(name = "token")
    @Setter
    private String token;

    @Column(name = "profile_img")
    private String profileImg;

    /* OAuth 필드 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;
    /* OAuth 필드 끝 */

    public User update(UserRequest.UpdateDTO updateDTO) {
        this.department = updateDTO.getDepartment();
        this.position = updateDTO.getPosition();
        this.name = updateDTO.getName();
        this.profileImg = updateDTO.getProfileImg();
        return this;
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserDetail userDetail;

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
        userDetail.setUser(this); // 양방향 관계 설정
    }

}
