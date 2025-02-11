package com.ssafy.exhi.domain.demand.repository;

import com.ssafy.exhi.domain.demand.model.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DemandRepository extends JpaRepository<Demand, Long> {
}
