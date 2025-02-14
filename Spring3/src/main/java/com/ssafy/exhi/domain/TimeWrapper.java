package com.ssafy.exhi.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class TimeWrapper {
    private LocalDateTime currentDateTime = LocalDateTime.now();
    
    public LocalDateTime now() {
        return currentDateTime;
    }
    
    public LocalDate today() {
        return currentDateTime.toLocalDate();
    }
    
    // 시간 조작 메서드들
    public void setCurrentTime(LocalDateTime dateTime) {
        this.currentDateTime = dateTime;
    }
    
    public void addDays(long days) {
        currentDateTime = currentDateTime.plusDays(days);
    }
    
    public void addHours(long hours) {
        currentDateTime = currentDateTime.plusHours(hours);
    }
    
    public void reset() {
        currentDateTime = LocalDateTime.now();
    }
}