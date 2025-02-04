package com.ssafy.exhi.domain.tip.model.model;


import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tip {
    @Id
    @GeneratedValue
    @Column(name = "tip_id")
    private Integer id;

    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "gender")
    private Gender gender;

    @Column(name = "tip")
    private String tip;

}