package com.ssafy.exhi.domain.user.repository;

import com.ssafy.exhi.domain.user.model.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserById(Integer userId);

    Optional<User> findUserByLoginId(String loginId);

    boolean existsUserByLoginId(String loginId);

    boolean existsUserById(Integer userId);

    /* --------------------- TOKEN ---------------------*/

    @Query(value = "SELECT u.token FROM User u WHERE u.id = :loginId")
    String getRefreshToken(Long loginId);

    @Query("SELECT u FROM User u WHERE LOWER(u.loginId) LIKE LOWER(CONCAT('%', :loginId, '%'))")
    Page<User> findUsersByLoginId(@Param("loginId") String loginId, Pageable pageable);

}
