package com.ssafy.exhi.domain.user.repository;

import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository  // ✅ 필수
public interface UserDetailRepository extends JpaRepository<UserDetail, Integer> {
    Optional<UserDetail> findByUserId(Integer userId);  // ✅ 올바른 쿼리 메서드

    boolean existsByUser(User user);
    // spring data jpa 메소드 명명법
}




