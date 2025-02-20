package com.ssafy.exhi.domain.shop.converter;

import com.ssafy.exhi.domain.shop.model.dto.ShopResponse;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.ItemDTO;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.ItemOptionDTO;
import com.ssafy.exhi.domain.shop.model.dto.ShopResponse.SimpleResultDTO;
import com.ssafy.exhi.domain.shop.model.entity.BusinessHours;
import com.ssafy.exhi.domain.shop.model.entity.Item;
import com.ssafy.exhi.domain.shop.model.entity.ItemOption;
import com.ssafy.exhi.domain.shop.model.entity.Shop;
import java.util.List;
import org.springframework.data.domain.Page;

public class ShopConverter {

    public static ShopResponse.ItemDTO toItemDTO(Item item) {
        if (item == null) {
            return null;
        }

        List<ItemOptionDTO> itemOptions = item.getItemOptions().stream().map(ShopConverter::toItemOptionDTO).toList();
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .description(item.getDescription())
                .itemOptions(itemOptions)
                .build();
    }

    public static ShopResponse.ItemOptionDTO toItemOptionDTO(ItemOption itemOption) {
        if (itemOption == null) {
            return null;
        }

        return ShopResponse.ItemOptionDTO.builder()
                .id(itemOption.getId())
                .name(itemOption.getName())
                .max(itemOption.getMax())
                .price(itemOption.getPrice())
                .required(itemOption.isRequired())
                .build();
    }

    public static ShopResponse.SimpleResultDTO toSimpleDTO(Shop entity) {
        if (entity == null) {
            return null;
        }

        return ShopResponse.SimpleResultDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .mainImageUrl(entity.getImageUrl())
                .serviceType(entity.getServiceType())
                .address(entity.getAddress())
                .minPrice(entity.getMinPrice())
                .build();
    }

    // 추가 필요
    public static ShopResponse.DetailResultDTO toDetailDTO(Shop entity) {
        if (entity == null) {
            return null;
        }

        List<ItemDTO> itemList = entity.getItems().stream().map(ShopConverter::toItemDTO).toList();
        BusinessHours businessHours = entity.getBusinessHours();
        businessHours.createTimeSlot();

        return ShopResponse.DetailResultDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .mainImageUrl(entity.getImageUrl())
                .serviceType(entity.getServiceType())
                .address(entity.getAddress())
                .duration(entity.getDuration())
                .description(entity.getDescription())
                .minPrice(entity.getMinPrice())
                .businessHours(businessHours)
                .items(itemList)
                .build();
    }

    // 전체조회 및 추천서비스에서 List대신 Page로 쭉 나열
    public static ShopResponse.PageDTO toPageDTO(Page<Shop> page) {
        if (page == null) {
            return null;
        }

        List<SimpleResultDTO> dtoList = page.getContent().stream().map(
                ShopConverter::toSimpleDTO
        ).toList();

        return ShopResponse.PageDTO.builder()
                .contents(dtoList)
                .listSize(dtoList.size())
                .isFirstPage(page.isFirst())
                .isLastPage(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }
}
