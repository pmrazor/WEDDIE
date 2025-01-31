package com.ssafy.exhi.domain.user.converter;


import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.entity.Token;
import com.ssafy.exhi.domain.user.model.entity.User;

public class UserConverter {
    public static UserResponse.DetailResultDTO toDetailResultDTO(User user) {
        return UserResponse.DetailResultDTO.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .department(user.getDepartment())
                .position(user.getPosition())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .profileImg(user.getProfileImg())
                .provider(user.getProvider() != null ? user.getProvider().toString() : null)
                .build();
    }

    public static UserResponse.SimpleResultDTO toSimpleResultDTO(User user) {
        return UserResponse.SimpleResultDTO.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
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
                .department(signUpDTO.getDepartment())
                .position(signUpDTO.getPosition())
                .provider(signUpDTO.getProvider())
                .profileImg(signUpDTO.getProfileImg())
                .build();
    }

}
