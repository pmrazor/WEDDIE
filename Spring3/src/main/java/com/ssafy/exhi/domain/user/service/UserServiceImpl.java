package com.ssafy.exhi.domain.user.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.couple.repository.CoupleRepository;
import com.ssafy.exhi.domain.oauth.AuthProvider;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.dto.UserResponse.PageDTO;
import com.ssafy.exhi.domain.user.model.entity.Token;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import com.ssafy.exhi.domain.user.repository.UserDetailRepository;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ssafy.exhi.domain.user.converter.UserConverter.toUserDetailDTO;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final JWTUtil jwtUtil;

    @Override
    public UserResponse.TokenDTO login(UserRequest.LoginDTO memberDto) {
        // 로그인 아이디 확인
        User loginUser = getUserByLoginId(memberDto.getLoginId());
        log.info("after loginUser");
        validatePassword(isMatches(loginUser, memberDto));

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

        log.info("savedUser: {}", user);
        User savedUser = userRepository.save(user);

        return UserConverter.toSimpleResultDTO(savedUser);
    }

    @Override
    public UserResponse.DuplicateLoginIdDto duplicateIdCheck(String loginId) {
        boolean isAvailable = !userRepository.existsUserByLoginId(loginId);

        UserResponse.DuplicateLoginIdDto result = new UserResponse.DuplicateLoginIdDto();
        result.setAvailable(isAvailable);

        return result;
    }

    @Override
    public UserResponse.DetailResultDTO getMyInfo(Integer userId) {
        User user = getUserById(userId);
        return UserConverter.toDetailResultDTO(user);
    }

    // 회원가입시 입력하는 회원 기본 정보 수정
    @Override
    public UserResponse.DetailResultDTO update(Integer userId, UserRequest.UpdateDTO userRequest) {
        User user = getUserById(userId);

        // OAuth 사용자와 로컬 사용자를 구분하여 처리
        if (user.getProvider() == AuthProvider.LOCAL) {
            // 로컬 사용자의 경우에만 비밀번호 검증
            validatePassword(isMatches(user, userRequest));
            // 새 비밀번호로 업데이트
            user.setPassword(passwordEncoder.encode(userRequest.getNewPassword()));
        }

        user.update(userRequest);

        return UserConverter.toDetailResultDTO(user);
    }

    // 유저 기본 정보 수정
    private User getUserInfoById(Integer userId) {
        return userRepository.findUserById(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_INFO_NOT_FOUND)
            );
    }

    public UserResponse.UserInfoDTO updateUserInfo(Integer userId, UserRequest.UpdateInfoDTO userInfoRequest) {
        User user = getUserInfoById(userId);

        // 기존 정보와 동일한 경우 업데이트하지 않음
        if (user.isSameInfo(userInfoRequest)) {
            throw new ExceptionHandler(ErrorStatus.USER_INFO_NO_CHANGES);
        }

        user.updateInfo(userInfoRequest);
        return UserConverter.toUserInfoDTO(user);
    }

    // 유저 상세 정보 수정
    private UserDetail updateUserDetailById(Integer userId) {
        return userDetailRepository.findByUserId(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND)
            );
    }

    public UserResponse.UserDetailDTO updateUserDetail(Integer userId, UserRequest.UpdateDetailDTO userDetailRequest) {
        UserDetail userDetail = updateUserDetailById(userId);

        // 기존 정보와 비교하여 변경 사항이 없으면 예외 발생
        if (userDetail.isSameDetail(userDetailRequest)) {
            throw new ExceptionHandler(ErrorStatus.USER_DETAIL_NO_CHANGES);
        }

        // 저장 후 반환
        userDetail.updateDetail(userDetailRequest);
        return toUserDetailDTO(userDetail);
    }

    // 유저 상세 정보 생성 (온보딩)
    private UserDetail setUserDetailById(Integer userId) {
        return userDetailRepository.findByUserId(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
    }

    public UserResponse.UserDetailDTO createUserDetail(Integer userId,
                                                       UserRequest.CreateDetailDTO createDetailRequest) {

        // 기존 UserDetail을 가져와서 존재 여부 확인
        UserDetail existingUserDetail = userDetailRepository.findByUserId(userId).orElse(null);

        // 만약 기존 UserDetail이 있으면, 새로 생성하지 않음
        if (existingUserDetail != null) {
            // ErrorStatus에 정의된 'USER_DETAIL_ALREADY_EXISTS' 에러 코드 사용
            throw new ExceptionHandler(ErrorStatus.USER_DETAIL_ALREADY_EXISTS);  // 이미 존재하면 예외 발생
        }

        UserDetail newUserDetail = new UserDetail();

        // `userId`를 사용하여 `User` 객체를 가져오기
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));

        // User 객체를 UserDetail 객체에 설정 (user_id 설정)
        newUserDetail.setUser(user); // user_id 설정

        newUserDetail.createDetail(createDetailRequest);
        userDetailRepository.save(newUserDetail);

        return toUserDetailDTO(newUserDetail);
    }

    // 유저 상세 정보 조회 (온보딩)
    private UserDetail getUserDetailById(Integer userId) {
        return userDetailRepository.findByUserId(userId)
            .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_DETAIL_NOT_FOUND)
            );
    }

    public UserResponse.UserDetailDTO getUserDetail(Integer userId) {
        UserDetail userDetail = getUserDetailById(userId);
        return UserConverter.toUserDetailDTO(userDetail);
    }

    @Override
    public boolean checkUser(String loginId) {
        return userRepository.existsUserByLoginId(loginId);
    }

    @Override
    public void delete(Integer userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    @Override
    public boolean existUserDetail(Integer userId) {
        User user = getUserById(userId);
        return userDetailRepository.existsByUser(user);
    }

    @Override
    public PageDTO searchOtherGenderUser(String loginId, Pageable pageable) {
        Page<User> page = userRepository.findUsersByLoginId(loginId, pageable);
        return UserConverter.toPageDTO(page);
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

    private void validatePassword(boolean loginUser) {
        // 비밀번호 일치 확인
        if (!loginUser) {
            throw new ExceptionHandler(ErrorStatus.USER_LOGIN_FAILED);
        }
    }

    private boolean isMatches(User user, UserRequest.LoginDTO memberDto) {
        return passwordEncoder.matches(memberDto.getPassword(), user.getPassword());
    }

    private boolean isMatches(User user, UserRequest.UpdateDTO memberDto) {
        return passwordEncoder.matches(memberDto.getOldPassword(), user.getPassword());
    }
}
