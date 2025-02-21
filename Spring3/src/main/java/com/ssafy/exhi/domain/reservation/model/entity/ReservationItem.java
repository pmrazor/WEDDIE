package com.ssafy.exhi.domain.reservation.model.entity;

import com.ssafy.exhi.domain.shop.model.entity.Item;
import com.ssafy.exhi.domain.shop.model.entity.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItem {

    @Id //
    @Column(name = "reservation_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // 1:N 예약 시 선택할 수 있는 개별 항목
    @OneToMany(mappedBy = "reservationItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationOption> reservationOptions = new ArrayList<>();

    public static ReservationItem of(Item item) {

        return ReservationItem.builder()
                .reservationOptions(new ArrayList<>())
                .item(item)
                .build();
    }

    public Money getTotalPrice() {
        Money price = item.getPrice();
        if (reservationOptions.isEmpty()) {
            return price;
        }

        Money optionPrice = calculateOptionPrice();

        return optionPrice.plus(price);
    }

    private Money calculateOptionPrice() {
        return reservationOptions.stream()
                .map(ReservationOption::getOptionPrice)
                .reduce(Money.ZERO, Money::plus);
    }

    public void addOption(ReservationOption option) {
        if (option == null) {
            return;
        }

        option.updateReservationItem(this);

        if (!reservationOptions.contains(option)) {
            reservationOptions.add(option);
        }

    }
}
