package com.ssafy.exhi.domain.shop.repository;

import com.ssafy.exhi.domain.shop.model.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Page<Shop> findAll(Pageable pageable);

    @Query("""
        SELECT s, COUNT(r) as reservationCount
        FROM Shop s
        LEFT JOIN Reservation r ON r.shop = s
        LEFT JOIN r.couple c
        LEFT JOIN c.coupleCluster cc
        WHERE cc.clusterId IN (
            SELECT cc2.clusterId
            FROM Couple c2
            JOIN c2.coupleCluster cc2
            WHERE c2.id = :coupleId
        )
        OR cc.clusterId IS NULL
        GROUP BY s
        ORDER BY COUNT(r) DESC
        """)
    List<Object[]> findRecommendedShopsByCluster(@Param("coupleId") Integer coupleId);
}
