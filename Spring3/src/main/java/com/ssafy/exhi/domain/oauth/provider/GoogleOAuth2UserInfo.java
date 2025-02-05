package com.ssafy.exhi.domain.oauth.provider;

import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
// equals() 와 hashCode() 를 자동 생성. 부모 클래스 필드 포함.
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    public String getAttribute(String code) {
        return (String) attributes.get(code);
    }

    @Override
    public String getProviderId() {
        return getAttribute("sub");
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
        return getAttribute("picture");
    }
}
