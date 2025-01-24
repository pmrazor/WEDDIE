package com.ssafy.exhi.domain.user.repository;


import com.ssafy.exhi.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserById(Integer id);

    Optional<User> findUserByUserId(String loginId);

    boolean existsUserByUserId(String loginId);

    boolean existsUserById(Integer userId);

    /* --------------------- TOKEN ---------------------*/

    @Query(value = "SELECT u.token FROM User u WHERE u.id = :userId")
    String getRefreshToken(Long userId);

}
