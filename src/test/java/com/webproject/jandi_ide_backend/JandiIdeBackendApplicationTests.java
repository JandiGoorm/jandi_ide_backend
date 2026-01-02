package com.webproject.jandi_ide_backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 애플리케이션 컨텍스트 로드 테스트
 * 
 * CI 환경에서는 DB 연결이 없으므로 이 테스트를 건너뜁니다.
 * 로컬 환경에서만 실행됩니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class JandiIdeBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
