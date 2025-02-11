package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.recommendation.model.entity.CoupleClusteringParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleClusteringParameterRepository extends JpaRepository<CoupleClusteringParameter, Integer> {
    Optional<CoupleClusteringParameter> findFirstByOrderByCreatedAtDesc();
}
