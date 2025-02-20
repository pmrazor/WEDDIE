package com.ssafy.exhi.domain.reservation.model.entity;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationHours {
    // getter 메서드들
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static ReservationHours of(LocalDateTime startTime, LocalDateTime endTime) {
        // 시작 시간이 종료 시간보다 늦으면 안 됨
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
        }

        return ReservationHours.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    /**
     * 현재 예약 시간이 다른 예약 시간과 겹치는지 확인합니다.
     *
     * @param other 비교할 다른 예약 시간
     * @return 겹치는 경우 true, 겹치지 않는 경우 false
     */
    public boolean isOverlapping(ReservationHours other) {
        if (other == null) {
            return false;
        }

        boolean isCurrentEndBeforeOtherStart = endTime.isBefore(other.startTime);
        boolean isCurrentStartAfterOtherEnd = startTime.isAfter(other.endTime);

        return !(isCurrentEndBeforeOtherStart || isCurrentStartAfterOtherEnd);
    }
}