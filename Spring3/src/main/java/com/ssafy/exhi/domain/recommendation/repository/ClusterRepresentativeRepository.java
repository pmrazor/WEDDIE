package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.recommendation.model.entity.ClusterRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClusterRepresentativeRepository extends JpaRepository<ClusterRepresentative, Integer> {
    // 클러스터 ID로 대표 사용자 조회
    Optional<ClusterRepresentative> findByClusterId(Integer clusterId);

} 