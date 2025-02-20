package com.ssafy.exhi.domain.shop.model.entity;

import com.ssafy.exhi.base.BaseEntity;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.reservation.model.entity.Reservation;
import com.ssafy.exhi.exception.ExceptionHandler;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Shop extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Integer id; // pk

    @Column(name = "name")
    private String name; // 숍이름

    @Embedded
    @Column(name = "address")
    private Address address; // 주소

    @Column(name = "duration")
    private Integer duration; // 진행 시간

    @Column(name = "description")
    private String description; // 상세설명

    @Column(name = "image_name")
    private String imageName;

    @Transient
    @Setter
    private Money minPrice;

    @Setter
    @Transient
    private String imageUrl; // 숍리스트에 띄워줄 사진 url

    @Embedded
    @Column(name = "business_hours")
    private BusinessHours businessHours; // 영업시간

    @Getter
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType; // 숍은 서비스 타입을 가진다. (웨딩홀, 스튜디오, 드레스 등)

    // 숍1 : 예약N 숍은 예약할 수 있다.
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    // 숍1: 아이템N 숍은 여러 개의 예약을 가진다.
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();

    /* 비즈니스 로직 */

    // 현재 시간
    public void validateBusinessHours(LocalDateTime startTime) {
        if (businessHours.isWithinBusinessHours(startTime)) {
            throw new ExceptionHandler(ErrorStatus._BAD_REQUEST);
        }
    }

    public LocalDateTime calculateEndTime(LocalDateTime startTime) {
        return startTime.plusMinutes(duration);
    }
}
