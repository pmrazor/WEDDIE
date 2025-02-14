package com.ssafy.exhi.domain.virginroad.repository;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.recommendation.model.dto.RoadPlanCapsuleDTO;
import com.ssafy.exhi.domain.virginroad.model.entity.VirginRoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VirginRoadRepository extends JpaRepository<VirginRoad, Integer> {

    Optional<VirginRoad> getVirginRoadByCouple(Couple couple);

    boolean existsVirginRoadByCouple(Couple couple);

    @Query("""
            SELECT new com.ssafy.exhi.domain.recommendation.model.dto.RoadPlanCapsuleDTO(
                vr.couple.id,
                vr.id,
                vr.virginRoadStatus,
                p.id,
                p.serviceType,
                p.planStatus,
                tc.id,
                tc.planGrade
            )
            FROM VirginRoad vr
                JOIN vr.plans p
                LEFT JOIN p.timeCapsules tc
            WHERE vr.couple.id IN :coupleIds
        """)
    List<RoadPlanCapsuleDTO> findAllMinimalDataByCoupleIds(@Param("coupleIds") List<Integer> coupleIds);

    Optional<VirginRoad> findVirginRoadByCoupleId(Integer coupleId);
}