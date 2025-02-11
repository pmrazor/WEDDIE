package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.recommendation.model.entity.CoupleCluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleClusterRepository extends JpaRepository<CoupleCluster, Integer> {
    Optional<CoupleCluster> findByCouple(Couple couple);
    Optional<CoupleCluster> findByCoupleId(Integer coupleId);
}
