package com.ssafy.exhi.domain.shop.repository;

import com.ssafy.exhi.domain.shop.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    Item findItemById(Integer itemId);
}
