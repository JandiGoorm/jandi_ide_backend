package com.webproject.jandi_ide_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정 클래스
 * 
 * HTTP 클라이언트를 Bean으로 등록하여 커넥션 풀을 재사용한다.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate Bean 생성
     * 
     * @return 설정된 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);  // 10초
        factory.setReadTimeout(30_000);     // 30초
        return new RestTemplate(factory);
    }
}
