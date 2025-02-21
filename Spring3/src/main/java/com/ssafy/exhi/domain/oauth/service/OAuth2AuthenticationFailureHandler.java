package com.ssafy.exhi.domain.oauth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("OAuth 인증 실패: {}", exception.getMessage());

        String errorMessage;
        Throwable cause = exception.getCause();

        if (exception instanceof OAuth2AuthenticationException &&
                exception.getMessage() != null && exception.getMessage().contains("이미")) {
            errorMessage = exception.getMessage();
        }else if (cause != null) {
            // 다른 예외의 경우 cause 메시지 사용
            errorMessage = cause.getMessage();
        } else {
            // 기본 에러 메시지
            errorMessage = "로그인 처리 중 오류가 발생했습니다. 다시 시도해주세요.";
        }

        log.error("Error Message: {}", errorMessage);

        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String targetUrl = UriComponentsBuilder.fromUriString("https://weddie.ssafy.me/login")
                .queryParam("error", encodedMessage)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
