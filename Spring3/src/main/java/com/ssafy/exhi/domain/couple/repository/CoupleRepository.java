package com.ssafy.exhi.domain.couple.repository;

import com.ssafy.exhi.domain.couple.model.entity.Couple;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Integer> {

    @Query(
            "SELECT c " +
                    "FROM Couple c " +
                    "LEFT JOIN FETCH c.female " +
                    "LEFT JOIN FETCH c.male " +
                    "WHERE c.female.id = :userId OR c.male.id = :userId"
    )
    Optional<Couple> findCoupleByUserId(@Param("userId") Integer userId);

    @Query(
            "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
                    "FROM Couple c " +
                    "WHERE c.female.id IN (:userId1, :userId2) OR c.male.id IN (:userId1, :userId2)"
    )
    boolean existsCoupleByUserIds(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);


    // for RecommendationServiceImpl
    /**
     * 같은 clusterId를 가진 커플 중, 제외할 ID를 뺀 뒤
     * 최대 100명의 커플을 무작위(RAND())로 불러옵니다.
     */
    @Query(value = """
            SELECT c.*
            FROM couples c
            JOIN couple_cluster cc ON cc.couple_id = c.couple_id
            WHERE cc.cluster_id = :clusterId
              AND c.couple_id != :excludeId
            ORDER BY RAND()
            LIMIT 100
            """, nativeQuery = true)
    List<Couple> findRandomSimilarCouples(@Param("clusterId") Integer clusterId,
                                          @Param("excludeId") Integer excludeId);
}
