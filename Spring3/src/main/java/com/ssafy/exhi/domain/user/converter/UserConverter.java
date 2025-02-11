package com.ssafy.exhi.domain.user.converter;

import com.ssafy.exhi.domain.oauth.AuthProvider;
import com.ssafy.exhi.domain.oauth.provider.OAuth2UserInfo;
import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.dto.UserResponse.PageDTO;
import com.ssafy.exhi.domain.user.model.entity.Token;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.model.entity.UserDetail;
import java.util.List;
import org.springframework.data.domain.Page;

public class UserConverter {
    public static UserResponse.DetailResultDTO toDetailResultDTO(User user) {
        return UserResponse.DetailResultDTO.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .profileImg(user.getProfileImg())
                .provider(user.getProvider() != null ? user.getProvider().toString() : null)
                .build();
    }

    public static UserResponse.SimpleResultDTO toSimpleResultDTO(User user) {
        return UserResponse.SimpleResultDTO.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .profileImg(user.getProfileImg())
                .provider(user.getProvider() != null ? user.getProvider().toString() : null)
                .build();
    }

    public static UserResponse.TokenDTO toTokenDTO(Token token) {
        return UserResponse.TokenDTO.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    public static UserResponse.TokenDTO toTokenDTO(String refreshToken) {
        return UserResponse.TokenDTO.builder()
                .refreshToken(refreshToken)
                .build();
    }

    public static User toEntity(UserRequest.SignUpDTO signUpDTO) {
        return User.builder()
                .loginId(signUpDTO.getLoginId())
                .password(signUpDTO.getPassword())
                .name(signUpDTO.getName())
                .provider(signUpDTO.getProvider())
                .profileImg(signUpDTO.getProfileImg())
//                .gender(signUpDTO.getGender())
                .build();
    }

    // 유저 기본 정보 변환 (gender, profileImg 포함)
    public static UserResponse.UserInfoDTO toUserInfoDTO(User user) {
        return UserResponse.UserInfoDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .loginId(user.getLoginId())
                .profileImg(user.getProfileImg())
                .build();
    }

    // 유저 상세 정보 변환 (age, phone, mbti 포함)
    public static UserResponse.UserDetailDTO toUserDetailDTO(UserDetail userDetail) {
        return UserResponse.UserDetailDTO.builder()
                .id(userDetail.getId())
                .age(userDetail.getAge())
                .gender(userDetail.getGender().toString())
                .phone(userDetail.getPhone())
                .mbti(userDetail.getMbti())
                .build();
    }

    public static User toOAuthEntity(String providerName, OAuth2UserInfo oAuth2UserInfo) {
        return User.builder()
                .provider(AuthProvider.valueOf(providerName))
                .providerId(oAuth2UserInfo.getProviderId())
                .name(oAuth2UserInfo.getName())
                .loginId(oAuth2UserInfo.getEmail())
                .profileImg(oAuth2UserInfo.getImageUrl())
                .build();
    }

    public static PageDTO toPageDTO(Page<User> page) {
        List<UserResponse.SimpleResultDTO> dtoList = page.getContent().stream().map(
                UserConverter::toSimpleResultDTO
        ).toList();

        return UserResponse.PageDTO.builder()
                .contents(dtoList)
                .listSize(dtoList.size())
                .isFirstPage(page.isFirst())
                .isLastPage(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }
}
