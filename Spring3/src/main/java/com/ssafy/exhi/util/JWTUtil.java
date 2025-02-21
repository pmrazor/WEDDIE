package com.ssafy.exhi.util;


import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.domain.user.model.entity.Token;
import com.ssafy.exhi.domain.user.model.entity.User;
import com.ssafy.exhi.exception.ExceptionHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Component
@Slf4j
public class JWTUtil {
    //
    @Value("#{${jwt.access-token.expiretime} * 60 * 1000}")
    private int accessTokenExpireTime;

    @Value("#{${jwt.refresh-token.expiretime} * 5 * 60 * 1000}")
    private int refreshTokenExpireTime;

    @Value("${jwt.secret-key}")
    private String secretKey;

    public Token createJwtToken(User user) {
        return Token.builder()
                .accessToken(
                        create(user, "accessToken", accessTokenExpireTime)
                )
                .refreshToken(
                        create(user, "refreshToken", refreshTokenExpireTime)
                )
                .build();
    }
    public Token createAccessToken(User user) {
        return Token.builder()
                .accessToken(
                        create(user, "accessToken", accessTokenExpireTime)
                )
                .refreshToken(null)
                .build();
    }

    //	AccessToken에 비해 유효기간을 길게 설정.
    public Token createRefreshToken(User user) {
        return Token.builder()
                .accessToken(null)
                .refreshToken(
                        create(user, "refreshToken", refreshTokenExpireTime)
                )
                .build();
    }

    public Integer getUserId(String header) {
        String token = getToken(header);
        Jws<Claims> claims = getClaimsJws(token);

        Map<String, Object> value = claims.getBody();
        log.info("value : {}", value);

        return Integer.parseInt(value.get("id").toString());
    }

    public Integer getCoupleId(String header) {
        String token = getToken(header);
        Jws<Claims> claims = getClaimsJws(token);


        Map<String, Object> value = claims.getBody();
        log.info("value : {}", value);

        return Integer.parseInt(value.get("coupleId").toString());
    }

    public String getUserLoginId(String header) {
        String token = getToken(header);
        Jws<Claims> claims = getClaimsJws(token);

        Map<String, Object> value = claims.getPayload();
        log.info("value : {}", value);

        return value.get("loginId").toString();
    }

    public void checkTokenValidation(String header) {
        if (header == null) throw new ExceptionHandler(ErrorStatus.TOKEN_NOT_FOUND);
        String token = getToken(header);
        Jws<Claims> claimsJws = getClaimsJws(token);
        claimsJws.getPayload().forEach((key1, value1) -> log.info("key : {}, value : {}", key1, value1));
    }

    private static String getToken(String header) {
        StringTokenizer st = new StringTokenizer(header);
        String token = st.nextToken();

        return "Bearer".equals(token) ? st.nextToken() : token;
    }

    //	Token 발금
    private String create(User user, String subject, long expireTime) {
        SecretKey key = getSecretKey();
        Map<String, String> headers = new HashMap<>();
        headers.put("typ", "JWT");

        String jwt = Jwts.builder()
                .header()
                .add(headers)
                .and()
                .subject(subject)
                .claim("coupleId", user.getCoupleId())
                .claim("loginId", user.getLoginId())
                .claim("name", user.getName())
                .claim("id", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        return jwt;
    }

    // 토큰 유효성 검사
    private Jws<Claims> getClaimsJws(String token) {
        JwtParser parser = getParser();
        Jws<Claims> claimsJws;

        try {
            claimsJws = parser.parseSignedClaims(token);
        } catch (Exception e) {
            log.error("토큰이 유효하지 않습니다.");
            ErrorStatus errorStatus = switch (e.getClass().getSimpleName()) {
                case "ExpiredJwtException" -> ErrorStatus.TOKEN_EXPIRED_ERROR;
                case "UnsupportedJwtException" -> ErrorStatus.TOKEN_UNSUPPORTED_ERROR;
                case "MalformedJwtException" -> ErrorStatus.TOKEN_MALFORMED_ERROR;
                case "SignatureException" -> ErrorStatus.TOKEN_SIGNATURE_ERROR;
                case "IllegalArgumentException" -> ErrorStatus.TOKEN_ILLEGAL_ARGUMENT_ERROR;
                default -> ErrorStatus.TOKEN_ERROR; // 알 수 없는 예외 처리
            };
            throw new ExceptionHandler(errorStatus);
        }
        return claimsJws;
    }

    private SecretKey getSecretKey() {
        byte[] secretKeyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }

    private JwtParser getParser() {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build();
    }

}
