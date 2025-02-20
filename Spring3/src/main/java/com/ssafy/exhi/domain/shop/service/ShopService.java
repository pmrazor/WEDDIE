package com.ssafy.exhi.domain.shop.service;

import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.DetailResultDTO;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.PageDTO;
import com.ssafy.exhi.domain.shop.model.entity.Shop;
import org.springframework.data.domain.Pageable;

public interface ShopService {
    PageDTO getAllShops(Pageable pageable);

    DetailResultDTO getShop(Integer shopId);

    Shop getRecommendShop(Integer userId, Pageable pageable);
}
