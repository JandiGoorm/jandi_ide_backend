package com.webproject.jandi_ide_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 모든 요청에 대해 인증 없이 접근을 허용합니다.
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                // CSRF 보호가 필요하지 않다면 비활성화합니다.
                .csrf(csrf -> csrf.disable())
                // 기본 로그인 폼 등을 사용하지 않도록 설정합니다.
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable());
        return http.build();
    }
}