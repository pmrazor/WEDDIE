package com.ssafy.exhi.domain.oauth.provider;

import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null) {
            return (String) properties.get("nickname");
        }

        Map<String, Object> profile = getKakaoAccountProfile();
        return profile != null ? (String) profile.get("nickname") : null;
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null) {
            return (String) properties.get("profile_image");
        }

        Map<String, Object> profile = getKakaoAccountProfile();
        return profile != null ? (String) profile.get("profile_image_url") : null;
    }

    private Map<String, Object> getKakaoAccountProfile() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
    }
}
