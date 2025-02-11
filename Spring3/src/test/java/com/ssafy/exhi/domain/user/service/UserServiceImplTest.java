package com.ssafy.exhi.domain.user.service;

import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void login() {
        // given
        UserRequest.SignUpDTO user = UserRequest.SignUpDTO.builder()
                .loginId("wnsgh712")
                .password("q!w2e3r4t5")
                .build();
        userService.register(user);

        UserRequest.LoginDTO member = UserRequest.LoginDTO.builder()
                .loginId("wnsgh712")
                .password("q!w2e3r4t5")
                .build();
        // when
        UserResponse.TokenDTO login = userService.login(member);

        // then
        assertThat(login).isNotNull();
    }

    @Test
    void register() {
        // given
        UserRequest.SignUpDTO user = UserRequest.SignUpDTO.builder()
                .loginId("wnsgh712")
                .password("q!w2e3r4t5")
                .build();

        // when
        UserResponse.SimpleResultDTO register = userService.register(user);

        // then
        assertThat(register).isNotNull();
    }

    @Test
    void getMyInfo() {
        // given
        UserRequest.SignUpDTO user = UserRequest.SignUpDTO.builder()
                .loginId("wnsgh712")
                .password("q!w2e3r4t5")
                .build();
        UserResponse.SimpleResultDTO register = userService.register(user);

        // when
        UserResponse.DetailResultDTO myInfo = userService.getMyInfo(register.getId());

        // then
        assertThat(myInfo).isNotNull();
    }

    @Test
    void update() {
        // given
        UserRequest.SignUpDTO register = UserRequest.SignUpDTO.builder()
                .name("parkjunho")
                .loginId("wnsgh712")
                .password("q!w2e3r4t5")
                .build();

        UserResponse.SimpleResultDTO user = userService.register(register);

        UserRequest.UpdateDTO dest = UserRequest.UpdateDTO.builder()
                .oldPassword("q!w2e3r4t5")
                .newPassword("q!w2e3r4t5y6")
                .build();

        // when
        UserResponse.DetailResultDTO update = userService.update(user.getId(), dest);

    }

    @Test
    void checkUser() {
        // given
        UserRequest.SignUpDTO register = UserRequest.SignUpDTO.builder()
                .name("parkjunho")
                .loginId("wnsgh712")
                .password("q!w2e3r4t5")
                .build();

        UserResponse.SimpleResultDTO user = userService.register(register);
        // when
        userService.checkUser("wnsgh712");

        // then
    }

    @Test
    void delete() {
        // given
        UserRequest.SignUpDTO register = UserRequest.SignUpDTO.builder()
                .name("parkjunho")
                .loginId("wnsgh712")
                .password("q!w2e3r4t5")
                .build();

        UserResponse.SimpleResultDTO user = userService.register(register);

        // when
        userService.delete(user.getId()); // 삭제 메서드 호출

        // then
        // 삭제 후 데이터가 존재하지 않는지 확인
        Assertions.assertFalse(userRepository.existsById(user.getId()));
    }

}