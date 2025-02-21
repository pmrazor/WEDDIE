package com.ssafy.exhi.domain.oauth.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    // Google 의 고유 ID (sub)
    public abstract String getProviderId();
    // 사용자 이름
    public abstract String getName();
    // 사용자 이메일 ( loginId 로 사용 )
    public abstract String getEmail();
    // 사용자 프로필 이미지 URL
    public abstract String getImageUrl();
}