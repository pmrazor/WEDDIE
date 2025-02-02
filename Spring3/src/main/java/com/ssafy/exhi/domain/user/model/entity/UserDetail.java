package com.ssafy.exhi.domain.user.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
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

    // url
    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "mbti")
    private String mbti;
}
