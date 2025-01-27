package com.ssafy.exhi.domain.virginroad.repository;

import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirginRoadRepository extends JpaRepository<VirginRoad, Integer> {

}
