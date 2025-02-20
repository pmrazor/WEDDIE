package com.ssafy.exhi.domain.shop.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MapKeyColumn;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * BusinessHours 엔티티 - Shop의 요일별 영업시간 정보를 관리하는 임베디드 타입 - 각 요일별로 다른 영업시간 설정 가능 - 영업시간 겹침 방지 및 유효성 검증 로직 포함 * 영업시간을 알려주는
 * 것을 클래스로 만듦
 */
@Getter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class BusinessHours {
    @ElementCollection
    @MapKeyColumn(name = "weekly_schedule_key")
    @CollectionTable(name = "shop_business_hours")
    private Map<DayOfWeek, OpeningHours> weeklySchedule = new HashMap<>();

    // 특정 시간이 영업시간에 포함되는지 확인
    public boolean isWithinBusinessHours(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        OpeningHours hours = weeklySchedule.get(dayOfWeek);

        if (hours == null) {
            return false; // 해당 요일 영업시간 미설정
        }

        LocalTime time = dateTime.toLocalTime();
        return hours.isTimeWithinHours(LocalDateTime.from(time));
    }

    // 요일별 영업시간 설정
    public void setHoursForDay(DayOfWeek day, OpeningHours hours) {
        weeklySchedule.put(day, hours);
    }

    public void createTimeSlot() {
        weeklySchedule.forEach((day, openingHours) -> {
            openingHours.getAvailableTimeSlots();
        });
    }
}
