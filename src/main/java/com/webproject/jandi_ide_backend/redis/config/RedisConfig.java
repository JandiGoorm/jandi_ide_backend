package com.webproject.jandi_ide_backend.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // --- Redis 연결 정보 ---
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    /**
     * Redis 연결을 위한 ConnectionFactory 빈 설정 (Lettuce 사용)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);

        // 비밀번호가 설정되어 있으면 적용
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisStandaloneConfiguration.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    /**
     * Jackson ObjectMapper 설정 빈
     * Redis에 객체를 JSON으로 직렬화/역직렬화할 때 사용됩니다.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Java 8의 날짜/시간 타입(LocalDateTime 등)을 Jackson이 처리할 수 있도록 모듈 등록
        mapper.registerModule(new JavaTimeModule());
        // 날짜/시간을 타임스탬프(숫자) 대신 ISO-8601 형식의 문자열로 직렬화하도록 설정
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 참고: 타입 정보 포함 설정(activateDefaultTyping)은 여기서는 사용하지 않습니다.
        // ChatRoomService에서 ObjectMapper.convertValue를 사용하여 명시적으로 변환합니다.
        return mapper;
    }

    /**
     * Redis 작업을 위한 RedisTemplate 빈 설정
     * Key/HashKey는 String으로, Value/HashValue는 JSON(Jackson)으로 직렬화합니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // String 직렬화 설정 (Key, HashKey)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // JSON 직렬화 설정 (Value, HashValue) - 위에서 설정한 ObjectMapper 사용
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(objectMapper()); // objectMapper() 빈 주입 사용
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }

    /**
     * Redis Pub/Sub 메시지를 처리하기 위한 리스너 컨테이너 빈 설정
     * 실제 리스너 등록은 RedisSubscriber 클래스에서 @PostConstruct를 통해 이루어집니다.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // Subscriber에서 리스너를 추가하므로 여기서는 별도 설정 없음
        return container;
    }
}