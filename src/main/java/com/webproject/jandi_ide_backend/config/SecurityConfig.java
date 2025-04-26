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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                        // OPTIONS 요청 허용 (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 채팅 관련 요청은 인증 필요
                        .requestMatchers("/chat/**").authenticated()
                        // POST, PUT, DELETE 요청은 ADMIN 권한 필요
                        .requestMatchers(HttpMethod.POST, "/api/companies/**","/api/job-postings/**","/api/schedules/**","/api/problems/**","/api/test-cases/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/companies/**","/api/job-postings/**","/api/schedules/**","/api/problems/**","/api/test-cases/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/companies/**","/api/job-postings/**","/api/schedules/**","/api/problems/**","/api/test-cases/**").hasAnyRole("STAFF", "ADMIN")
                        // 관심 기업은 모두 가능
                        .requestMatchers("/api/companies/favorite", "/api/companies/favorite/**").permitAll()
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 명시적으로 특정 도메인 허용 (Netlify 도메인 포함)
        config.setAllowedOrigins(Arrays.asList(
            "https://jandiide.netlify.app", 
            "http://localhost:3000", 
            "http://localhost:5173"
        ));
        
        // 모든 출처 허용 패턴 - 위의 setAllowedOrigins와 함께 사용하지 않음
        // config.addAllowedOriginPattern("*");
        
        // 허용할 HTTP 메서드 설정
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 허용할 HTTP 헤더 설정
        config.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin", 
                "X-Requested-With", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        // 인증 정보(쿠키 등) 포함 여부
        config.setAllowCredentials(true);
        // 브라우저가 Access-Control-Allow-Headers에 대한 응답을 캐시하는 시간
        config.setMaxAge(3600L);
        // 브라우저에 노출할 헤더 설정
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
