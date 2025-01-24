package com.ssafy.exhi.domain.user.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@ToString
@Entity
@Schema(name = "User", description = "유저 엔티티")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "department")
    private String department;

    @Column(name = "position")
    private String position;

    @Column(name = "name")
    private String name;

    @Column(name = "user_id", unique = true)
    private String userId;

    @Column(name = "password")
    private String password;

    @Column(name = "token")
    @Setter
    private String token;

    public User update(UserRequest.UpdateDTO updateDTO) {
        this.department = updateDTO.getDepartment();
        this.position = updateDTO.getPosition();
        this.name = updateDTO.getName();
        this.password = updateDTO.getNewPassword();
        return this;
    }

}
