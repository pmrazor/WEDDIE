package com.ssafy.exhi.domain.user.controller;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.domain.oauth.UserPrincipal;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.service.UserService;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController implements UserControllerDocs{

    private final JWTUtil jwtUtil;
    private final UserService userService;

    @Override
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest.LoginDTO user) {
        log.info("login user: {}", user);
        // 로그인 확인하기
        UserResponse.TokenDTO userToken = userService.login(user);
        log.debug("login user : {}", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(userToken));
    }

    /**
     * 회원가입
     * 권한: 모두 사용 가능
     * @return
     */
    @Override
    @PostMapping("/users")
    public ResponseEntity<?> register(@RequestBody @Valid UserRequest.SignUpDTO user) {
        log.info("register user: {}", user);
        UserResponse.SimpleResultDTO register = userService.register(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(register));
    }

    /**
     * 내프로필[GET] /users/meRequesto없음ResponseoBody200
     * 권한: 로그인 한 사용자
     *
     * @return
     */
    @Override
    @GetMapping("/users/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String accessToken, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Integer userId = jwtUtil.getUserId(accessToken);

        log.info("me user : {}", userId);
        UserResponse.DetailResultDTO userDetail = userService.getMyInfo(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetail));
    }

    /**
     * 존재하는 회원 확인용 API
     * 권한: 모두 사용 가능
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        log.info("get user : {}", userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userService.checkUser(userId)));
    }

    /**
     * @param accessToken
     * @param userRequest
     * @return 권한: 로그인 한 사용자
     */
    @Override
    @PatchMapping("/users")
    public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String accessToken, @RequestBody @Valid UserRequest.UpdateDTO userRequest) {
        log.info("updateUser: {}", userRequest);
        Integer userId = jwtUtil.getUserId(accessToken);

        UserResponse.DetailResultDTO user = userService.update(userId, userRequest);

        return ResponseEntity.ok(ApiResponse.onSuccess(user));
    }

    /**
     *
     * @param accessToken
     * @return
     * 권한: 로그인 한 사용자
     */
    @Override
    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String accessToken) {
        Integer userId = jwtUtil.getUserId(accessToken);
        log.info("updateUser: {}", userId);

        userService.delete(userId);

        return ResponseEntity.noContent().build();

    }

}
