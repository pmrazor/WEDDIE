package com.ssafy.exhi.domain.plan.model.entity;

import com.ssafy.exhi.domain.ServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
    @GeneratedValue
    @Column(name = "tip_id")
    private Integer id;

    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "tip")
    private String tip;

}
