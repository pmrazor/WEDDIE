package com.ssafy.exhi.domain.shop.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String city;        // 도시
    private String district;    // 구/군
    private String detail;      // 상세주소 
    private String postalCode;  // 우편번호

    private Double latitude;    // 위도
    private Double longitude;   // 경도

    public String getFullAddress() {
        return city + " " + district + " " + detail;
    }

}