package com.ssafy.exhi.domain.oauth.service;

import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.oauth.UserPrincipal;
import com.ssafy.exhi.domain.user.model.entity.Token;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.exception.ExceptionHandler;
import com.ssafy.exhi.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication) throws IOException {

        log.info("onAuthenticationSuccess 호출");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.USER_NOT_FOUND));
        
        // 토큰 생성
        Token jwtToken = jwtUtil.createJwtToken(user);

        // 리프레시 토큰 저장
        user.setToken(jwtToken.getRefreshToken());

        userRepository.save(user);

        response.setHeader("Authorization", "Bearer " + jwtToken.getAccessToken());

        // 토큰과 사용자 정보를 함께 전달
        // URL 파라미터 인코딩
        String redirectUrl = UriComponentsBuilder.fromUriString("https://weddie.ssafy.me/oauth/callback")
                .queryParam("token", jwtToken.getAccessToken())
                .queryParam("email", user.getLoginId() != null ? user.getLoginId() : "")
                .queryParam("name", user.getName() != null ? user.getName() : "")
                .queryParam("profileImg", user.getProfileImg() != null ? user.getProfileImg() : "")
                .build()
                .encode()  // 한 번만 인코딩
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
