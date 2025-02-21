package com.ssafy.exhi.domain.oauth.provider;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.oauth.AuthProvider;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@UtilityClass
public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        log.info("getOAuth2UserInfo() 호출");

        if(registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            log.info("google 로 이동");
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.KAKAO.toString())) {
            log.info("kakao 로 이동");
            return new KakaoOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.NAVER.toString())) {
            log.info("naver 로 이동");
            return new NaverOAuth2UserInfo(attributes);
        } else {
            log.info("error");
            throw new ExceptionHandler(ErrorStatus._BAD_REQUEST);
        }
    }
}
