package com.ssafy.exhi.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class TimeWrapper {
    // 현재 시간 저장
    private LocalDateTime currentDateTime = LocalDateTime.now();

    // 현재 시간 반환
    public LocalDateTime now() {
        return currentDateTime;
    }

    // 현재 날짜'만' 반환
    public LocalDate today() {
        return currentDateTime.toLocalDate();
    }

    // 시간 조작 메서드들. 시간 설정 메서드 (외부에서 특정 시간 설정할 때)
    public void setCurrentTime(LocalDateTime dateTime) {
        this.currentDateTime = dateTime;
    }

    // 일수 더하는 메서드
    public void addDays(Integer days) {
        currentDateTime = currentDateTime.plusDays(days);
    }

    // 시간 더하는 메서드 (현재 시간 기준으로 시간 '만' 추가)
    public void addHours(long hours) {
        currentDateTime = currentDateTime.plusHours(hours);
    }

    // 시간을 초기화하여 현재 시간으로 되돌리는 메서드
    public void reset() {
        currentDateTime = LocalDateTime.now();
    }
}