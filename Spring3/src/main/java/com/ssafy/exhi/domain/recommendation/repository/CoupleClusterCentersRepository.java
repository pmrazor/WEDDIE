package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.recommendation.model.entity.CoupleClusterCenters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleClusterCentersRepository extends JpaRepository<CoupleClusterCenters, Integer> {
    Optional<CoupleClusterCenters> findFirstByOrderByCreatedAtDesc();
}
