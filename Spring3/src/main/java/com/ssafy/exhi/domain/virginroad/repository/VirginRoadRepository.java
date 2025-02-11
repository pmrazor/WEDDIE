package com.ssafy.exhi.domain.virginroad.repository;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirginRoadRepository extends JpaRepository<VirginRoad, Integer> {

    Optional<VirginRoad> getVirginRoadByCouple(Couple couple);

    boolean existsVirginRoadByCouple(Couple couple);
}
