package com.ssafy.exhi.domain.shop.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.notice.service.MinioImageService;
import com.ssafy.exhi.domain.reservation.repository.ReservationRepository;
import com.ssafy.exhi.domain.shop.converter.ShopConverter;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.DetailResultDTO;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.PageDTO;
import com.ssafy.exhi.domain.shop.model.entity.Item;
import com.ssafy.exhi.domain.shop.model.entity.Money;
import com.ssafy.exhi.domain.shop.model.entity.Shop;
import com.ssafy.exhi.domain.shop.repository.ShopRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final MinioImageService minioImageService;
    private final ShopRepository shopRepository;
    private final CoupleRepository coupleRepository;
    private final ReservationRepository reservationRepository;

    // 숍 전체 조회
    @Override
    public PageDTO getAllShops(Pageable pageable) {
        Page<Shop> page = shopRepository.findAll(pageable);

        page.getContent().forEach(this::updateShop);

        return ShopConverter.toPageDTO(page);
    }

    // 숍 상세 조회
    @Override
    public DetailResultDTO getShop(Integer shopId) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.SHOP_NOT_FOUND)
        );
        updateShop(shop);
        return ShopConverter.toDetailDTO(shop);
    }

    // 숍 추천(기본 2개)
    @Override
    public PageDTO getRecommendShop(Integer userId, Pageable pageable) {
        Couple couple = coupleRepository.findCoupleByUserId(userId)
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.COUPLE_NOT_FOUND));

        // 클러스터 기반으로 추천된 숍 목록 가져오기
        List<Object[]> recommendedShopsWithCount = shopRepository.findRecommendedShopsByCluster(couple.getId());
        
        List<Shop> recommendedShops = recommendedShopsWithCount.stream()
                .map(array -> (Shop) array[0])
                .collect(Collectors.toList());

        // ServiceType별로 그룹화하고 최대 2개씩 선택
        Map<ServiceType, List<Shop>> groupedShops = recommendedShops.stream()
                .collect(Collectors.groupingBy(Shop::getServiceType));

        List<Shop> limitedRecommendations = groupedShops.values().stream()
                .flatMap(shops -> shops.stream().limit(2))
                .collect(Collectors.toList());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), limitedRecommendations.size());

        List<Shop> pageContent = (start > limitedRecommendations.size())
                ? Collections.emptyList()
                : limitedRecommendations.subList(start, end);

        Page<Shop> pageResult = new PageImpl<>(pageContent, pageable, limitedRecommendations.size());

        pageResult.getContent().forEach(this::updateShop);
        return ShopConverter.toPageDTO(pageResult);
    }

    private void updateShop(Shop shop) {
        shop.setImageUrl(minioImageService.getImageUrl(shop.getImageName()));
        shop.setMinPrice(shop.getItems().stream()
                .map(Item::getPrice)
                .min(Comparator.comparing(Money::getAmount))
                .orElse(Money.ZERO));
    }

}
