package com.ssafy.exhi.domain.user.service;

import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.dto.UserResponse.PageDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    // 인증 (로그인, 회원가입)
    // 로그인 요청시 JWT 토큰 반환
    UserResponse.TokenDTO login(UserRequest.LoginDTO memberDto);

    // 회원가입 시 새 유저 저장 후 ID, 이름 반환
    UserResponse.SimpleResultDTO register(UserRequest.SignUpDTO memberDto);

    // 회원가입 시, 아이디 중복 검사
    UserResponse.DuplicateLoginIdDto duplicateIdCheck(String loginId);

    // 유저 정보 조회 및 수정
    // 유저 기본 정보 조회
    UserResponse.DetailResultDTO getMyInfo(Integer userId);

    // 비밀번호 포함한 계정 정보 수정 (Oauth 검증 포함)
    UserResponse.DetailResultDTO update(Integer userId, UserRequest.@Valid UpdateDTO userRequest);

    // 기본정보 수정 gender, profileimg
    UserResponse.UserInfoDTO updateUserInfo(Integer userId, UserRequest.UpdateInfoDTO userRequest);

    // 상세정보 수정 age, phone, mbti
    UserResponse.UserDetailDTO updateUserDetail(Integer userId, UserRequest.UpdateDetailDTO userDetailRequest);

    // 상세정보 생성 (온보딩)
    UserResponse.UserDetailDTO createUserDetail(Integer userId, UserRequest.CreateDetailDTO createDetailRequest);

    // 상세정보 조회 (온보딩)
    UserResponse.UserDetailDTO getUserDetail(Integer userId);

    boolean checkUser(String loginId);

    void delete(Integer userId);

    boolean existUserDetail(Integer userId);

    PageDTO searchOtherGenderUser(String loginId, Pageable pageable);
}
