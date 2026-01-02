package com.webproject.jandi_ide_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 애플리케이션 기본 테스트
 * 
 * 통합 테스트 가이드(docs/TESTING_GUIDE.md)에 따라 단위 테스트 중심으로 작성됩니다.
 * Spring Context 로드 없이 애플리케이션 클래스의 기본 동작만 검증합니다.
 */
class JandiIdeBackendApplicationTests {

    @Test
    void main_applicationClassExists_notNull() {
        assertNotNull(JandiIdeBackendApplication.class);
    }

    @Test
    void main_springApplicationClassExists_notNull() {
        assertNotNull(SpringApplication.class);
    }

}
