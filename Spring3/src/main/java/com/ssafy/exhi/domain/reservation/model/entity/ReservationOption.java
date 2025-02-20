package com.ssafy.exhi.domain.reservation.model.entity;

import com.ssafy.exhi.domain.shop.model.entity.ItemOption;
import com.ssafy.exhi.domain.shop.model.entity.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationOption {
    @Id
    @Column(name = "reservation_option_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_option_id", nullable = false)
    private ItemOption itemOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_item_id", nullable = false)
    private ReservationItem reservationItem;

    public static ReservationOption of(ItemOption option, Integer quantity, ReservationItem reservationItem) {
        return ReservationOption.builder()
                .reservationItem(reservationItem)
                .itemOption(option)
                .quantity(quantity)
                .build();
    }

    public Money getOptionPrice() {
        return itemOption.getPrice().multiply(quantity);
    }

    public void updateReservationItem(ReservationItem reservationItem) {
        if (reservationItem != null) {
            reservationItem.getReservationOptions().remove(this);
        }

        this.reservationItem = reservationItem;
    }

}
