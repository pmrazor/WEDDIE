package com.ssafy.exhi.domain.couplemail.repository;

import com.ssafy.exhi.domain.couplemail.model.entity.CoupleMail;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoupleMailRepository extends JpaRepository<CoupleMail, Integer> {

    // 특정 커플의 받은 메일 목록 조회
    @Query("SELECT m FROM CoupleMail m " +
            "WHERE m.couple.id = :coupleId AND m.receiver.id = :userId " +
            "ORDER BY m.createdAt DESC")
    List<CoupleMail> findReceivedMails(@Param("coupleId") Integer coupleId,
                                       @Param("userId") Integer userId);

    // 메일 상세 조회 (권한 검증 포함)
    @Query("SELECT m FROM CoupleMail m " +
            "WHERE m.id = :mailId " +
            "AND (m.sender.id = :userId OR m.receiver.id = :userId)")
    Optional<CoupleMail> findMailWithAuth(@Param("mailId") Integer mailId,
                                          @Param("userId") Integer userId);
}