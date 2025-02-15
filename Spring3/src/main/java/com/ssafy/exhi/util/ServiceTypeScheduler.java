package com.ssafy.exhi.util;

import com.ssafy.exhi.domain.ServiceType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceTypeScheduler {
    public Map<ServiceType, Double> getServiceTypeSchedule() {
        Map<ServiceType, Double> scheduleDays = new HashMap<>();

        scheduleDays.put(ServiceType.WEDDING_HALL, 0.95);   // 웨딩홀
        scheduleDays.put(ServiceType.DRESS_SHOP, 0.5);    // 드레스샵
        scheduleDays.put(ServiceType.STUDIO, 0.4);        // 스튜디오 촬영
        scheduleDays.put(ServiceType.SNAP, 0.3);         // 스냅 촬영
        scheduleDays.put(ServiceType.INVITATION, 0.25);   // 청첩장
        scheduleDays.put(ServiceType.HANBOK, 0.2);        // 한복
        scheduleDays.put(ServiceType.TAILOR_SHOP, 0.15);   // 남성 예복
        scheduleDays.put(ServiceType.FACIAL_CARE, 0.1);   // 피부 관리
        scheduleDays.put(ServiceType.MAKEUP_STUDIO, 0.05); // 메이크업
        scheduleDays.put(ServiceType.HONEYMOON, 0.02);     // 신혼여행 준비
        scheduleDays.put(ServiceType.WEDDING_DAY, 0.0);    // 결혼식 당일

        return scheduleDays;
    };
};