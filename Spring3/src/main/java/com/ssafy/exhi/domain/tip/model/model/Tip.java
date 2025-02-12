package com.ssafy.exhi.domain.tip.model.model;

import com.ssafy.exhi.domain.ServiceType;
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

    @Column(name = "tip", length = 2000)
    private String tip;
}