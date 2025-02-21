package com.ssafy.exhi.domain.reservation.model.entity;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.shop.model.entity.Money;
import com.ssafy.exhi.domain.shop.model.entity.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {
    @Id
    @Column(name = "reservation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    private ReservationHours reservationHours; // 예약시간

    private Money totalAmount; // 금액

    @ColumnDefault("'PENDING'")
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus; // enum 예약상태

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType; // enum 서비스타입

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_item_id")
    private ReservationItem reservationItem;

    public static Reservation of(
            Shop shop,
            Couple couple,
            LocalDateTime startTime,
            ReservationItem reservationItem
    ) {
        validateReservationTime(shop, startTime);
        ReservationHours hours = getReservationHours(shop, startTime);

        return Reservation.builder()
                .shop(shop)
                .couple(couple)
                .reservationHours(hours)
                .reservationItem(reservationItem)
                .serviceType(shop.getServiceType())
                .reservationStatus(ReservationStatus.PENDING)
                .build();
    }

    private static ReservationHours getReservationHours(Shop shop, LocalDateTime startTime) {
        LocalDateTime endTime = shop.calculateEndTime(startTime);
        return ReservationHours.of(startTime, endTime);
    }

    // 예약 시간 유효성 검증
    private static void validateReservationTime(Shop shop, LocalDateTime startTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("과거 시간으로 예약할 수 없습니다.");
        }

        shop.validateBusinessHours(startTime);
    }

    // 가격 계산
    private static Money getTotalPrice(ReservationItem reservationItem) {
        return reservationItem.getTotalPrice();
    }

    // 예약 취소
    public void cancel(LocalDateTime cancelTime) {
        validateCancellationPeriod(cancelTime);
        this.reservationStatus = ReservationStatus.CANCELLED;
    }

    private void validateCancellationPeriod(LocalDateTime cancelTime) {
        // 3일전까지만 취소 가능하다.
        LocalDate reservationDate = cancelTime.toLocalDate();
        LocalDate currentDate = LocalDate.now();

        long daysUntilReservation = ChronoUnit.DAYS.between(currentDate, reservationDate);

        if (daysUntilReservation < 3) {
            throw new IllegalStateException("예약은 3일 전까지만 취소 가능합니다.");
        }
    }

    // 예약 확정
    public void confirm() {
        this.reservationStatus = ReservationStatus.RESERVED;
    }

}