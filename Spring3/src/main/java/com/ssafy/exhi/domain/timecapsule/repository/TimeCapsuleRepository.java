package com.ssafy.exhi.domain.timecapsule.repository;

import com.ssafy.exhi.domain.timecapsule.model.entity.TimeCapsule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeCapsuleRepository extends JpaRepository<TimeCapsule, Integer> {
    List<TimeCapsule> findAllByCoupleId(Integer coupleId);
    boolean existsByUserIdAndPlanId(Integer userId, Integer planId);
}
