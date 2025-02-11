package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.recommendation.model.entity.UserClusterCenters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserClusterCentersRepository extends JpaRepository<UserClusterCenters, Integer> {
    Optional<UserClusterCenters> findFirstByOrderByCreatedAtDesc();
}
