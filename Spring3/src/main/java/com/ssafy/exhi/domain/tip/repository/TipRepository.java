package com.ssafy.exhi.domain.tip.repository;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.tip.model.entity.Tip;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipRepository extends JpaRepository<Tip, Integer> {
    List<Tip> findTipsByServiceTypeAndUserDetail_Gender(ServiceType serviceType, Gender gender);
}
