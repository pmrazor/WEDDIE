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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController implements UserControllerDocs {

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
     * 회원가입 권한: 모두 사용 가능
     *
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
     * 내프로필[GET] /users/meRequesto없음ResponseoBody200 권한: 로그인 한 사용자
     *
     * @return
     */
    @Override
    @GetMapping("/users/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String accessToken,
                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Integer userId = jwtUtil.getUserId(accessToken);

        log.info("me user : {}", userId);
        UserResponse.DetailResultDTO userDetail = userService.getMyInfo(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetail));
    }

    /**
     * 중복 아이디 확인용 API 권한: 모두 사용 가능
     *
     * @return
     */
    @Override
    @GetMapping("/users/checkId")
    public ResponseEntity<?> checkDuplicateLoginId(@RequestParam("loginId") String loginId) {
        log.info("check for id : {}", loginId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userService.duplicateIdCheck(loginId)));
    }

    /**
     * 존재하는 회원 확인용 API 권한: 모두 사용 가능
     *
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        log.info("get user : {}", userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userService.checkUser(userId)));
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> searchUser(
            @RequestParam("loginId") String loginId,
            Pageable pageable
    ) {
        UserResponse.PageDTO response = userService.searchOtherGenderUser(loginId, pageable);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));

    }

    /**
     * 회원가입시 입력하는 회원 정보 수정 API
     *
     * @param accessToken
     * @param userRequest
     * @return 권한: 로그인 한 사용자
     */
    @Override
    @PatchMapping("/users")
    public ResponseEntity<?> updateUser(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid UserRequest.UpdateDTO userRequest) {

        log.info("updateUser: {}", userRequest);
        Integer userId = jwtUtil.getUserId(accessToken);

        UserResponse.DetailResultDTO user = userService.update(userId, userRequest);

        return ResponseEntity.ok(ApiResponse.onSuccess(user));
    }

    // 회원가입 시 입력하는 유저 기본 정보 수정 (profileImg)
    @PatchMapping("/users/info")
    public ResponseEntity<?> updateUserInfo(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid UserRequest.UpdateInfoDTO userRequest) { // Valid 필수

        log.info("updateUserInfo: {}", userRequest);
        Integer userId = jwtUtil.getUserId(accessToken);

        UserResponse.UserInfoDTO updatedUserInfo = userService.updateUserInfo(userId, userRequest);

        return ResponseEntity.ok(ApiResponse.onSuccess(updatedUserInfo));
    }

    // 온보딩 시 입력하는 유저 상세 정보 수정 (age, gender, phone, mbti)
    @PatchMapping("/users/detail")
    public ResponseEntity<?> updateUserDetail(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid UserRequest.UpdateDetailDTO userDetailRequest) {

        log.info("updateUserDetail: {}", userDetailRequest);
        Integer userId = jwtUtil.getUserId(accessToken);

        UserResponse.UserDetailDTO updatedUserDetail = userService.updateUserDetail(userId, userDetailRequest);

        return ResponseEntity.ok(ApiResponse.onSuccess(updatedUserDetail));
    }

    // 온보딩 시 입력하는 유저 상세 정보 생성 (age, gender, phone, mbti)
    @PostMapping("/users/detail")
    public ResponseEntity<?> createUserDetail(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid UserRequest.CreateDetailDTO createDetailRequest) {

        log.info("createUserDetail: {}", createDetailRequest);
        Integer userId = jwtUtil.getUserId(accessToken);

        UserResponse.UserDetailDTO newUserDetail = userService.createUserDetail(userId, createDetailRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(newUserDetail));
    }

    // 온보딩 시 입력하는 유저 상세 정보 확인 (age, gender, phone, mbti)
    @GetMapping("/users/detail")
    public ResponseEntity<?> getUserDetail(
            @RequestHeader("Authorization") String accessToken) {

        log.info("getUserDetail: {}", accessToken);
        Integer userId = jwtUtil.getUserId(accessToken);

        UserResponse.UserDetailDTO getUserDetail = userService.getUserDetail(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(getUserDetail));
    }

    // 유저 상세 정보가 없는 유저의 수정시도 시 오류 처리 API
    @PostMapping("/users/detail/flag")
    public ResponseEntity<?> existUserDetail(
            @RequestHeader("Authorization") String accessToken
    ) {

        Integer userId = jwtUtil.getUserId(accessToken);
        boolean flag = userService.existUserDetail(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess(flag));
    }

    /**
     * @param accessToken
     * @return 권한: 로그인 한 사용자
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
