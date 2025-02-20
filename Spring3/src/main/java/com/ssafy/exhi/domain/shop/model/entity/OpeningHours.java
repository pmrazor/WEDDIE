package com.ssafy.exhi.domain.shop.model.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * - 하루의 영업 시작 시간과 종료 시간을 관리 - 시간 간격 유효성 검증 및 시간 비교 로직 포함 출력받으면 1
 */
@Getter
@Embeddable
public class OpeningHours {
    private LocalDateTime openTime; // 오픈시간
    private LocalDateTime closeTime; // 닫는시간

    @Transient
    private List<LocalDateTime> timeSlots = new ArrayList<>(); // 예약 가능한 시간 슬롯 리스트

    // 영업시간 유효성 검증
    private void validateHours() {
        if (openTime.isAfter(closeTime)) {
            throw new IllegalArgumentException("영업 시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
    }

    // 특정 시간이 영업시간 내인지 확인
    public boolean isTimeWithinHours(LocalDateTime time) {
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    // 예약 가능한 시간 슬롯 생성 (1시간 단위)
    public void getAvailableTimeSlots() {
        if (timeSlots == null) {
            this.timeSlots = new ArrayList<>();
        }
        LocalDateTime currentTime = openTime;

        while (currentTime.isBefore(closeTime)) {
            timeSlots.add(currentTime);
            currentTime = currentTime.plusHours(1);
        }

    }
}