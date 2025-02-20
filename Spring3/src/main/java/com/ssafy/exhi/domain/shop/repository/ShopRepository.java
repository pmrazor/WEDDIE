package com.ssafy.exhi.domain.shop.repository;

import com.ssafy.exhi.domain.shop.model.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Page<Shop> findAll(Pageable pageable);

//    // 특정 ServiceType에 해당하는 숍 조회
//    @Query("SELECT s FROM Shop s WHERE s.serviceType = :serviceType ORDER BY s.createdAt DESC")
//    Page<Shop> findByServiceType(ServiceType serviceType, Pageable pageable);
}
