package com.ssafy.exhi.domain.shop.model.dto;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.shop.model.entity.Address;
import com.ssafy.exhi.domain.shop.model.entity.BusinessHours;
import com.ssafy.exhi.domain.shop.model.entity.Money;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class ShopResponse {

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    // 전체조회시 사용
    public static class SimpleResultDTO {
        private Integer id; // pk
        private String name; // 숍이름
        private String mainImageUrl;
        private ServiceType serviceType;
        private Address address; // 주소
        private Money minPrice; // 최저 가격
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    // 상세조회시 사용
    public static class DetailResultDTO {
        private Integer id; // pk
        private String name; // 숍이름
        private String mainImageUrl;
        private ServiceType serviceType;
        private Address address;
        private Integer duration;
        private String description;
        private Money minPrice; // 최저 가격
        private BusinessHours businessHours; // 영업시간
        private List<ItemDTO> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageDTO {
        private List<SimpleResultDTO> contents;
        private Integer listSize;
        private boolean isFirstPage;
        private boolean isLastPage;
        private Integer totalPages;
        private Long totalElements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {
        private Integer id;
        private String name;
        private Money price;
        private String description;
        private List<ItemOptionDTO> itemOptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemOptionDTO {
        private Integer id;
        private String name;
        private Money price;
        private Integer max;
        private boolean required;
    }

}
