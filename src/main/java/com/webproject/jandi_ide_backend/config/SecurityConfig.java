package com.webproject.jandi_ide_backend.config;

import com.webproject.jandi_ide_backend.security.JwtAuthenticationFilter;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // WebSocket 연결 허용
                        .requestMatchers("/ws/**").permitAll()
                        // Actuator 엔드포인트 허용
                        .requestMatchers("/actuator/health").permitAll()
                        // Swagger UI 및 API 문서 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 로그인, 리프레시 토큰 요청 허용
                        .requestMatchers("/api/users/login", "/api/users/refresh").permitAll()
                        // 관심 기업 관련 요청 허용 - 특별히 지정
                        .requestMatchers("/api/companies/favorite", "/api/companies/favorite/**").authenticated()
                        // 채팅 관련 요청은 인증 필요
                        .requestMatchers("/chat/**").authenticated()

                        // 기업 관련 요청 - 관심 기업 제외
                        .requestMatchers(HttpMethod.POST, "/api/companies", "/api/companies/{id}/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/companies", "/api/companies/{id}/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/companies", "/api/companies/{id}/**").hasAnyRole("STAFF", "ADMIN")
                        
                        // 나머지 관리자 권한 필요 요청
                        .requestMatchers(HttpMethod.POST, "/api/job-postings/**","/api/schedules/**","/api/problems/**","/api/test-cases/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/job-postings/**","/api/schedules/**","/api/problems/**","/api/test-cases/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/job-postings/**","/api/schedules/**","/api/problems/**","/api/test-cases/**").hasAnyRole("STAFF", "ADMIN")
                        
                        // 나머지 API 요청은 인증 필요
                        .requestMatchers("/api/**").authenticated()
                        // 그 외 모든 요청 허용
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    response.setStatus(401);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}");
                                },
                                new AntPathRequestMatcher("/api/**")
                        )
                );
        return http.build();
    }
}
