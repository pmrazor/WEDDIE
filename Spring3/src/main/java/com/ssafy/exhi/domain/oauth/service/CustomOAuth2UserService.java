package com.ssafy.exhi.domain.oauth.service;

import com.ssafy.exhi.domain.oauth.AuthProvider;
import com.ssafy.exhi.domain.oauth.UserPrincipal;
import com.ssafy.exhi.domain.oauth.provider.OAuth2UserInfo;
import com.ssafy.exhi.domain.oauth.provider.OAuth2UserInfoFactory;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // http://localhost:8080/oauth2/authorization/google
    // http://localhost:8080/oauth2/authorization/kakao
    // http://localhost:8080/oauth2/authorization/naver

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    /*
        구글에서 받아온 사용자 정보 처리하는 진입점
        super.loadUser 로 구글에서 사용자 정보 가져옴
        가져온 정보를 processOAuth2User 로 넘김  
    */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        log.info("LoadUser() 호출");
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /*
        구글에서 받아온 정보를 웨디에 맞게 변환
        사용자 있는지, 있으면 업데이트 없으면 새로 등록하는 곳
    */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        log.info("processOAuth2User() 호출");
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes()
        );

        // 이메일로 사용자 찾기
        Optional<User> userOptional = userRepository.findUserByLoginId(oAuth2UserInfo.getEmail());
        log.info("Searching for user with email: {}", oAuth2UserInfo.getEmail());
        log.info("User exists: {}", userOptional.isPresent());

        User user;
        if(userOptional.isPresent()) {
            User existingUser = userOptional.get();
            // 다른 OAuth 제공자로 가입하려는 경우
            if (!existingUser.getProvider().toString().equalsIgnoreCase(
                    oAuth2UserRequest.getClientRegistration().getRegistrationId())) {
                throw new OAuth2AuthenticationException(
                        String.format("이미 %s로 가입된 계정입니다. %s로 로그인해주세요.",
                                existingUser.getProvider(),
                                existingUser.getProvider())
                );
            }
            user = updateExistingUser(existingUser, oAuth2UserInfo);
            log.info("Updated existing user: {}", user);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
            log.info("Registered new user: {}", user);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    // 사용자 DB 저장
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {

        // registrationId를 대문자로 변환하여 enum 값과 매칭
        String providerName = oAuth2UserRequest.getClientRegistration()
                .getRegistrationId().toUpperCase();

        User user = UserConverter.toOAuthEntity(providerName, oAuth2UserInfo);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());

        return userRepository.save(existingUser);
    }
}
