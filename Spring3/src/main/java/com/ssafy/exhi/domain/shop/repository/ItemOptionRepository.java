package com.ssafy.exhi.domain.shop.repository;

import com.ssafy.exhi.domain.shop.model.entity.ItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOptionRepository extends JpaRepository<ItemOption, Integer> {
}
