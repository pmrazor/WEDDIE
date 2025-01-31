package com.ssafy.exhi.domain.couple.repository;

import com.ssafy.exhi.domain.couple.model.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {

    @Query(
            "SELECT c " +
            "FROM Couple c " +
            "LEFT JOIN FETCH c.female " +
            "LEFT JOIN FETCH c.male " +
            "WHERE c.female.id = :userId OR c.male.id = :userId"
    )
    Optional<Couple> findCoupleByUserId(@Param("userId") Integer userId);
}
