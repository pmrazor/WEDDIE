package com.ssafy.exhi.domain.user.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.entity.Token;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Override
    public UserResponse.TokenDTO login(UserRequest.LoginDTO memberDto) {
        // 로그인 아이디 확인
        User loginUser = getUserByLoginId(memberDto.getLoginId());
        validatePassword(isMatches(loginUser, memberDto), ErrorStatus.USER_LOGIN_FAILED);

        // 토큰 생성
        Token jwtToken = jwtUtil.createJwtToken(loginUser);

        // Refresh Token만 DB에 저장
        loginUser.setToken(jwtToken.getRefreshToken());

        return UserConverter.toTokenDTO(jwtToken);
    }



    @Override
    public UserResponse.SimpleResultDTO register(UserRequest.SignUpDTO memberDto) {
        if (userRepository.existsUserByLoginId(memberDto.getLoginId())) {
            throw new ExceptionHandler(ErrorStatus.USER_DUPLICATED_LOGIN_ID);
        }

        memberDto.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        User user = UserConverter.toEntity(memberDto);

        User savedUser = userRepository.save(user);

        return UserConverter.toSimpleResultDTO(savedUser);
    }

    @Override
    public UserResponse.DetailResultDTO getMyInfo(Integer userId) {
        User user = getUserById(userId);
        return UserConverter.toDetailResultDTO(user);
    }

    @Override
    public UserResponse.DetailResultDTO update(Integer userId, UserRequest.UpdateDTO userRequest) {
        User user = getUserById(userId);

        // Request의 비밀번호가 일치하는지 확인하기
        validatePassword(isMatches(user, userRequest), ErrorStatus.USER_LOGIN_FAILED);
        user.update(userRequest);

        return UserConverter.toDetailResultDTO(user);
    }

    @Override
    public boolean checkUser(String userId) {
        return userRepository.existsUserByLoginId(userId);
    }

    @Override
    public void delete(Integer userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    private User getUserByLoginId(String userId) {
        return userRepository.findUserByLoginId(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
        );
    }

    private User getUserById(Integer userId) {
        return userRepository.findUserById(userId).orElseThrow(
                () -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND)
        );
    }

    private void validatePassword(boolean loginUser, ErrorStatus userLoginFailed) {
        // 비밀번호 일치 확인
        if (!loginUser) {
            throw new ExceptionHandler(userLoginFailed);
        }
    }

    private boolean isMatches(User user, UserRequest.LoginDTO memberDto) {
        return passwordEncoder.matches(memberDto.getPassword(), user.getPassword());
    }

    private boolean isMatches(User user, UserRequest.UpdateDTO memberDto) {
        return passwordEncoder.matches(memberDto.getOldPassword(), user.getPassword());
    }
}
