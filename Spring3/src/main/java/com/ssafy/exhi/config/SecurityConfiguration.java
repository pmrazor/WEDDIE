package com.ssafy.exhi.config;

import com.ssafy.exhi.domain.oauth.service.CustomOAuth2UserService;
import com.ssafy.exhi.domain.oauth.service.OAuth2AuthenticationFailureHandler;
import com.ssafy.exhi.domain.oauth.service.OAuth2AuthenticationSuccessHandler;
import com.ssafy.exhi.domain.user.repository.UserRepository;
import com.ssafy.exhi.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity // SpringSecurity 활성화
@RequiredArgsConstructor
@Configuration
@Slf4j
public class SecurityConfiguration {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JWTUtil jwtUtil;

    // BCryptPasswordEncoder를 빈으로 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(new OAuth2AuthenticationSuccessHandler(jwtUtil, userRepository))
                .failureHandler(oAuth2AuthenticationFailureHandler))
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());
        return http.build();
    }
}
