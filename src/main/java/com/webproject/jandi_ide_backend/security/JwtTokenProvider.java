package com.webproject.jandi_ide_backend.security;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key secretKey;
    private final UserRepository userRepository;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret, UserRepository userRepository) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("jwtSecret: {}", jwtSecret);
        this.userRepository = userRepository;
    }

    /**
     * 깃헙 로그인 시 액세스 토큰을 생성합니다.
     *
     * @param githubId   깃헙 아이디
     * @param githubToken 깃헙 토큰
     * @return 생성된 액세스 토큰 문자열
     */
    public String createAccessToken(String githubId, String githubToken){
        log.info("토큰생성");

        // 1. 깃헙 유저 정보 조회
        User user =  userRepository.findByGithubId(githubId).orElse(null);

        // 2. 깃헙 유저 정보가 없으면 예외 처리
        if (user == null) {
            throw new CustomException(CustomErrorCodes.USER_NOT_FOUND);
        }

        Date now = new Date();
        // 15 분으로 설정
        long accessTokenExpiry = 15 * 60 * 1000;
        Date expiry = new Date(now.getTime() + accessTokenExpiry);
        String token = Jwts.builder()
                .setSubject(githubId) // 깃헙 아이디를 jwt 의 subject 에 저장
                .claim("githubToken",githubToken)  // 유저의 깃헙 토큰을 jwt 에 저장
                .setIssuedAt(now) // 토큰 발급 시간
                .setExpiration(expiry) // 토큰 만료 시간
                .signWith(secretKey)
                .compact();

        log.info("토큰 생성 완료");
        return token;
    }

    /**
     * 깃헙 로그인 시 리프레시 토큰을 생성합니다.
     *
     * @param githubId   깃헙 아이디
     * @param githubToken 깃헙 토큰
     * @return 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(String githubId, String githubToken) {
        log.info("리프레시 토큰 생성");

        Date now = new Date();
        // 7일로 설정
        long refreshTokenExpiry = 7 * 24 * 60 * 60 * 1000;
        Date expiry = new Date(now.getTime() + refreshTokenExpiry);
        String token = Jwts.builder()
                .setSubject(githubId) // 깃헙 아이디를 jwt 의 subject 에 저장
                .claim("githubToken",githubToken)  // 유저의 깃헙 토큰을 jwt 에 저장
                .setIssuedAt(now) // 토큰 발급 시간
                .setExpiration(expiry) // 토큰 만료 시간
                .signWith(secretKey)
                .compact();

        log.info("리프레시 토큰 생성 완료");
        return token;
    }

    /**
     * 액세스 토큰을 디코딩하여 깃헙 아이디와 깃헙 토큰을 추출합니다.
     *
     * @param token 디코딩할 액세스 토큰
     * @return 깃헙 아이디와 깃헙 토큰이 포함된 TokenInfo 객체
     * @throws CustomException 토큰이 유효하지 않을 경우 예외 발생
     */
    public TokenInfo decodeToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("디코딩된 Claims: {}", claims);

            String githubId = claims.getSubject();
            String githubToken = claims.get("githubToken", String.class);

            log.info("액세스 토큰 디코딩 완료: githubId={}", githubId);
            return new TokenInfo(githubId, githubToken);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new CustomException(CustomErrorCodes.EXPIRED_JWT_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }
    }
}
