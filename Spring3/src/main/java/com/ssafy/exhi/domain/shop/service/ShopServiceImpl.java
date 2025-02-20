package com.ssafy.exhi.domain.shop.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.notice.service.MinioImageService;
import com.ssafy.exhi.domain.shop.converter.ShopConverter;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.DetailResultDTO;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.PageDTO;
import com.ssafy.exhi.domain.shop.model.entity.Item;
import com.ssafy.exhi.domain.shop.model.entity.Money;
import com.ssafy.exhi.domain.shop.model.entity.Shop;
import com.ssafy.exhi.domain.shop.repository.ShopRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import java.util.Comparator;
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

    @Override
    public Shop getRecommendShop(Integer userId, Pageable pageable) {
        return null;
    }

    private void updateShop(Shop shop) {
        shop.setImageUrl(minioImageService.getImageUrl(shop.getImageName()));
        shop.setMinPrice(shop.getItems().stream()
                .map(Item::getPrice)
                .min(Comparator.comparing(Money::getAmount))
                .orElse(Money.ZERO));
    }

}
