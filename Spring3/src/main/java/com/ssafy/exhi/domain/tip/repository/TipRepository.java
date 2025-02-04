package com.ssafy.exhi.domain.tip.repository;

import com.ssafy.exhi.domain.ServiceType;
import com.ssafy.exhi.domain.tip.model.model.Tip;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipRepository extends JpaRepository<Tip, Long> {
    List<Tip> findTipsByServiceType(ServiceType serviceType);

    List<Tip> findTipsByServiceTypeAndGender(ServiceType serviceType, Gender gender);
}
