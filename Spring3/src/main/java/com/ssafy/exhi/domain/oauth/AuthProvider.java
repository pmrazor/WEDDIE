package com.ssafy.exhi.domain.oauth;

public enum AuthProvider {
    LOCAL, GOOGLE, KAKAO, NAVER;

    @Override
    public String toString() {
        // 구글 기본 제공 provider 가 소문자
        return name().toLowerCase();
    }
}