package com.ssafy.exhi.domain.couple.repository;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Integer> {

    @Query("""
        SELECT c
        FROM Couple c
        LEFT JOIN FETCH c.female
        LEFT JOIN FETCH c.male
        WHERE c.female.id = :userId OR c.male.id = :userId
    """)
    Optional<Couple> findCoupleByUserId(@Param("userId") Integer userId);

    @Query(value = """
        SELECT couple_id 
        FROM couples 
        WHERE female_id = :userId OR male_id = :userId
        """, nativeQuery = true)
    Integer findCoupleIdByUserId(@Param("userId") Integer userId);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Couple c
        WHERE c.female.id IN (:userId1, :userId2) OR c.male.id IN (:userId1, :userId2)
    """)
    boolean existsCoupleByUserIds(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    /**
     * 동일한 클러스터에 속한 유사 커플들을 최대 50개까지 랜덤으로 반환합니다.
     */
    @Query("""
        SELECT c
        FROM Couple c
        JOIN c.coupleCluster cc
        WHERE cc.clusterId = :clusterId
          AND c.id <> :excludeId
    """)
    Page<Couple> findRandomSimilarCouples(
            @Param("clusterId") Integer clusterId,
            @Param("excludeId") Integer excludeId,
            Pageable pageable
    );

}