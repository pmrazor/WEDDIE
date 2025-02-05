package com.ssafy.exhi.domain.recommendation.repository;

import com.ssafy.exhi.domain.recommendation.model.entity.UserCluster;
import com.ssafy.exhi.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserClusterRepository extends JpaRepository<UserCluster, Integer> {
    // 특정 사용자의 클러스터 정보 조회
    Optional<UserCluster> findByUser(User user);
    Optional<UserCluster> findByUserId(Integer userId);

} 