package com.ssafy.exhi.domain.oauth.provider;

import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
// equals() 와 hashCode() 를 자동 생성. 부모 클래스 필드 포함.
public class NaverOAuth2UserInfo extends OAuth2UserInfo {
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    public String getAttribute(String code) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return (String) response.get(code);
    }

    @Override
    public String getProviderId() {
        return getAttribute("id");
    }

    @Override
    public String getName() {
        return getAttribute("name");
    }

    @Override
    public String getEmail() {
        return getAttribute("email");
    }

    @Override
    public String getImageUrl() {
        return getAttribute("profile_image");
    }
}