package com.ssafy.exhi.domain.shop.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssafy.exhi.domain.reservation.model.entity.ReservationHours;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationHoursTest {

    @Test
    @DisplayName("오버랩핑 테스트")
    void overlappingTest() {
        // given
        LocalDateTime now = LocalDateTime.now();

        ReservationHours period1 = ReservationHours.of(now, now.plusHours(2));

        // when & then
        // 1. 겹치는 경우
        assertTrue(period1.isOverlapping(
                ReservationHours.of(now.plusHours(1), now.plusHours(3))

        ));

        // 2. 겹치지 않는 경우
        assertFalse(period1.isOverlapping(
                ReservationHours.of(now.plusHours(3), now.plusHours(4))
        ));

        // 3. 경계값(같은 시간) 테스트
        assertTrue(period1.isOverlapping(
                ReservationHours.of(now, now.plusHours(1))
        ));

        // 4. null 테스트
        assertFalse(period1.isOverlapping(null));
    }
}