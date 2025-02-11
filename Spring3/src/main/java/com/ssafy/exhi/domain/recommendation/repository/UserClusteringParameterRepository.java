package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.recommendation.model.entity.UserClusteringParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserClusteringParameterRepository extends JpaRepository<UserClusteringParameter, Integer> {
    Optional<UserClusteringParameter> findFirstByOrderByCreatedAtDesc();
}
