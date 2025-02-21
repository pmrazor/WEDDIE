package com.ssafy.exhi.domain.reservation.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.reservation.model.dto.ReservationRequest;
import com.ssafy.exhi.domain.reservation.model.dto.ReservationRequest.OptionDTO;
import com.ssafy.exhi.domain.reservation.model.entity.Reservation;
import com.ssafy.exhi.domain.reservation.model.entity.ReservationItem;
import com.ssafy.exhi.domain.reservation.model.entity.ReservationOption;
import com.ssafy.exhi.domain.reservation.repository.ReservationRepository;
import com.ssafy.exhi.domain.shop.model.entity.Item;
import com.ssafy.exhi.domain.shop.model.entity.ItemOption;
import com.ssafy.exhi.domain.shop.model.entity.Shop;
import com.ssafy.exhi.domain.shop.repository.ItemOptionRepository;
import com.ssafy.exhi.domain.shop.repository.ItemRepository;
import com.ssafy.exhi.domain.shop.repository.ShopRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;

    private final ShopRepository shopRepository;
    private final ItemRepository itemRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final CoupleRepository coupleRepository;

    // 예약 상세 조회
    public Reservation getReservation(Integer userId, Integer reservationId) {
        return reservationRepository.getReservationById(userId);
    }

    // 전체 예약 조회
    public List<Reservation> getAllReservations(Integer userId) {
        Couple couple = coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
        return reservationRepository.findAllByCouple(couple);
    }

    // 예약하기
    public Reservation save(Integer userId, ReservationRequest.CreateDTO createDTO) {

        Couple couple = coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));
        Shop shop = shopRepository.findById(createDTO.getShopId())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.SHOP_NOT_FOUND_ERROR));
        Item item = itemRepository.findById(createDTO.getItemId())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.ITEM_NOT_FOUND_ERROR));
        LocalDateTime reservationTime = createDTO.getReservationTime();
        List<OptionDTO> optionDTOs = createDTO.getOptions();

        ReservationItem reservationItem = ReservationItem.of(item);

        for (OptionDTO option : optionDTOs) {
            ItemOption itemOption = itemOptionRepository.findById(option.getOptionId())
                    .orElseThrow(() -> new ExceptionHandler(ErrorStatus.ITEM_NOT_FOUND_ERROR));
            ReservationOption reservationOption = ReservationOption.of(itemOption, option.getQuantity(),
                    reservationItem);
            reservationItem.addOption(reservationOption);
        }

        Reservation reservation = Reservation.of(shop, couple, reservationTime, reservationItem);

        return reservationRepository.save(reservation);
    }

}
