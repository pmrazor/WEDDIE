package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.recommendation.model.entity.ClusteringParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClusteringParameterRepository extends JpaRepository<ClusteringParameter, Integer> {
    Optional<ClusteringParameter> findFirstByOrderByCreatedAtDesc();
}
