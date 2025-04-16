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
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key secretKey;
    private final UserRepository userRepository;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret, UserRepository userRepository) {
        // 시크릿 키가 너무 짧으면 로그 경고
        if (jwtSecret.length() < 32) {
            log.warn("JWT 시크릿 키가 너무 짧습니다. 보안을 위해 32자 이상 사용하세요.");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.debug("JWT 시크릿 키 초기화 완료");
        this.userRepository = userRepository;
        this.accessTokenValidity = 3600000; // 1 hour
        this.refreshTokenValidity = 86400000; // 24 hours
    }

    /**
     * 깃헙 로그인 시 액세스 토큰을 생성합니다.
     *
     * @param githubId   깃헙 아이디
     * @param githubToken 깃헙 토큰
     * @return 생성된 액세스 토큰 문자열
     */
    public String createAccessToken(String githubId, String githubToken){
        log.debug("액세스 토큰 생성 시작: githubId={}", githubId);

        // 1. 깃헙 유저 정보 조회
        User user =  userRepository.findByGithubId(githubId).orElse(null);

        // 2. 깃헙 유저 정보가 없으면 예외 처리
        if (user == null) {
            log.error("사용자를 찾을 수 없음: githubId={}", githubId);
            throw new CustomException(CustomErrorCodes.USER_NOT_FOUND);
        }

        Date now = new Date();
        // 15 분으로 설정
        long accessTokenExpiry = 15 * 60 * 1000;
        Date expiry = new Date(now.getTime() + accessTokenExpiry);
        
        try {
            String token = Jwts.builder()
                    .setSubject(githubId) // 깃헙 아이디를 jwt 의 subject 에 저장
                    .claim("githubToken", githubToken)  // 유저의 깃헙 토큰을 jwt 에 저장
                    .claim("userId", user.getId())  // 사용자 ID 추가
                    .claim("role", user.getRole().name())  // 사용자 역할 추가
                    .setIssuedAt(now) // 토큰 발급 시간
                    .setExpiration(expiry) // 토큰 만료 시간
                    .signWith(secretKey)
                    .compact();

            log.debug("액세스 토큰 생성 완료: githubId={}, 만료시간={}", 
                    githubId, expiry.toString());
            return token;
        } catch (Exception e) {
            log.error("토큰 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(CustomErrorCodes.JWT_TOKEN_CREATION_ERROR);
        }
    }

    /**
     * 깃헙 로그인 시 리프레시 토큰을 생성합니다.
     *
     * @param githubId   깃헙 아이디
     * @param githubToken 깃헙 토큰
     * @return 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(String githubId, String githubToken) {
        log.debug("리프레시 토큰 생성 시작: githubId={}", githubId);

        Date now = new Date();
        // 7일로 설정
        long refreshTokenExpiry = 7 * 24 * 60 * 60 * 1000;
        Date expiry = new Date(now.getTime() + refreshTokenExpiry);
        
        try {
            String token = Jwts.builder()
                    .setSubject(githubId) // 깃헙 아이디를 jwt 의 subject 에 저장
                    .claim("githubToken", githubToken)  // 유저의 깃헙 토큰을 jwt 에 저장
                    .claim("tokenType", "refresh")      // 토큰 타입 추가
                    .setIssuedAt(now) // 토큰 발급 시간
                    .setExpiration(expiry) // 토큰 만료 시간
                    .signWith(secretKey)
                    .compact();

            log.debug("리프레시 토큰 생성 완료: githubId={}, 만료시간={}", 
                    githubId, expiry.toString());
            return token;
        } catch (Exception e) {
            log.error("리프레시 토큰 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(CustomErrorCodes.JWT_TOKEN_CREATION_ERROR);
        }
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

            log.debug("토큰 디코딩 성공: subject={}, 만료시간={}", 
                    claims.getSubject(), claims.getExpiration());

            String githubId = claims.getSubject();
            String githubToken = claims.get("githubToken", String.class);

            return new TokenInfo(githubId, githubToken);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw new CustomException(CustomErrorCodes.EXPIRED_JWT_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }
    }

    public String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return createToken(username, authorities, accessTokenValidity);
    }

    public String createRefreshToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return createToken(username, authorities, refreshTokenValidity);
    }

    private String createToken(String username, Collection<? extends GrantedAuthority> authorities, long validity) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + validity);

        try {
            return Jwts.builder()
                    .setSubject(username)
                    .claim("auth", authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()))
                    .setIssuedAt(now)
                    .setExpiration(validityDate)
                    .signWith(secretKey)
                    .compact();
        } catch (Exception e) {
            log.error("토큰 생성 중 오류: {}", e.getMessage(), e);
            throw new CustomException(CustomErrorCodes.JWT_TOKEN_CREATION_ERROR);
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Collection<? extends GrantedAuthority> authorities;
            
            // 권한 정보가 있는 경우
            if (claims.get("auth") != null) {
                try {
                    authorities = ((List<String>) claims.get("auth")).stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                } catch (Exception e) {
                    log.warn("Auth 클레임 처리 중 오류: {}, 기본 USER 권한 할당", e.getMessage());
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                }
            } 
            // 역할 정보가 있는 경우 (GitHub 로그인)
            else if (claims.get("role") != null) {
                String role = claims.get("role", String.class);
                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            }
            // 기본 권한
            else {
                log.warn("토큰에 권한 정보가 없습니다. 기본 USER 권한 할당");
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            org.springframework.security.core.userdetails.User principal = 
                    new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
            
            return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    principal, token, authorities);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰으로 인증 시도: {}", e.getMessage());
            throw new CustomException(CustomErrorCodes.EXPIRED_JWT_TOKEN);
        } catch (Exception e) {
            log.warn("인증 처리 중 오류: {}", e.getMessage());
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 중 오류: {}", e.getMessage());
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 토큰의 만료 시간을 확인합니다.
     * 
     * @param token JWT 토큰
     * @return 토큰 만료까지 남은 시간(초)
     */
    public long getTokenExpirationInSeconds(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            return Math.max(0, (expiration.getTime() - now.getTime()) / 1000);
        } catch (Exception e) {
            log.warn("토큰 만료 시간 확인 중 오류: {}", e.getMessage());
            return 0;
        }
    }
}
