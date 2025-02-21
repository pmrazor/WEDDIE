package com.ssafy.exhi.domain.shop.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse;
import com.ssafy.exhi.domain.shop.service.ShopService;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {
    private final JWTUtil jwtUtil;
    private final ShopService shopService;

    // 전체 숍 목록 조회
    @GetMapping
    public ResponseEntity<?> getAllShops(
            Pageable pageable
    ) {
        ShopResponse.PageDTO response = shopService.getAllShops(pageable);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    // 숍을 상세 조회
    @GetMapping("/{shopId}")
    public ResponseEntity<?> getShop(
            @PathVariable("shopId") Integer shopId
    ) {
        ShopResponse.DetailResultDTO response = shopService.getShop(shopId);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }

    @GetMapping("/recommend")
    public ResponseEntity<?> getRecommendShop(
            @RequestHeader(name = "Authorization") String accessToken,
            Pageable pageable
    ) {
        Integer userId = jwtUtil.getUserId(accessToken);
        ShopResponse.PageDTO response = shopService.getRecommendShop(userId, pageable);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(response));
    }
}
