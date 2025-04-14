package com.webproject.jandi_ide_backend.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.webproject.jandi_ide_backend.redis.pubsub.RedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 관련 설정을 위한 구성 클래스입니다.
 * Redis 연결, 데이터 직렬화/역직렬화 방식, Pub/Sub 메시징 설정 등을 정의합니다.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    /**
     * Redis 서버와의 연결을 관리하는 팩토리 빈(Bean)을 생성합니다.
     * application.properties에 설정된 호스트, 포트, 비밀번호 정보를 사용하여 연결합니다.
     *
     * @return RedisConnectionFactory 인스턴스 (Lettuce 기반)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis 연결 정보를 담는 객체 생성
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);

        // 비밀번호가 설정되어 있는 경우 (null 이거나 비어있지 않은 경우) 설정
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisStandaloneConfiguration.setPassword(redisPassword);
        }

        // LettuceConnectionFactory 생성 시 RedisStandaloneConfiguration 객체를 전달
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    /**
     * Jackson 라이브러리의 ObjectMapper 빈을 생성하고 커스터마이징합니다.
     * Redis에 객체를 JSON 형태로 저장하거나, JSON 데이터를 객체로 변환할 때 사용됩니다.
     *
     * @return 커스터마이징된 ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Java 8의 날짜/시간 타입(LocalDateTime 등)을 올바르게 직렬화/역직렬화하기 위한 모듈 등록
        mapper.registerModule(new JavaTimeModule());
        // 날짜/시간을 타임스탬프(숫자) 형태가 아닌 표준 ISO-8601 문자열 형태로 직렬화하도록 설정
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Redis 데이터 조작을 위한 핵심 도구인 RedisTemplate 빈을 생성하고 설정합니다.
     * Key, Value, Hash Key, Hash Value 등의 직렬화 방식을 지정합니다.
     *
     * @param factory Redis 연결 팩토리 (위에서 정의한 redisConnectionFactory() 빈이 주입됨)
     * @return 설정된 RedisTemplate 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // Redis 연결 팩토리 설정
        template.setConnectionFactory(factory);

        // Key 직렬화 방식: StringRedisSerializer 사용 (문자열로 저장)
        template.setKeySerializer(new StringRedisSerializer());
        // Value 직렬화 방식: Jackson2JsonRedisSerializer 사용 (객체를 JSON 문자열로 변환하여 저장)
        // 커스터마이징된 objectMapper를 사용하여 Java 객체(Object.class)를 처리
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        // Hash Key 직렬화 방식: StringRedisSerializer 사용
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value 직렬화 방식: Jackson2JsonRedisSerializer 사용
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return template;
    }

    /**
     * Redis Pub/Sub 기능을 위한 채널(토픽) 이름을 정의하는 빈을 생성합니다.
     * 발행자(Publisher)와 구독자(Subscriber)는 이 토픽 이름을 통해 메시지를 주고받습니다.
     *
     * @return "chatroom"이라는 이름을 가진 ChannelTopic 인스턴스
     */
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

    /**
     * Redis 메시지 리스너 컨테이너 빈을 생성하고 설정합니다.
     * 이 컨테이너는 특정 Redis 채널(토픽)을 구독하고 있다가,
     * 해당 채널로 메시지가 발행되면 등록된 리스너(Subscriber)에게 메시지를 전달하는 역할을 합니다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @param redisSubscriber   메시지를 수신하여 처리할 리스너 (RedisSubscriber 빈이 주입됨)
     * @param channelTopic      구독할 채널 토픽 (위에서 정의한 channelTopic() 빈이 주입됨)
     * @return 설정된 RedisMessageListenerContainer 인스턴스
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisSubscriber redisSubscriber,
            ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // Redis 연결 팩토리 설정
        container.setConnectionFactory(connectionFactory);
        // 지정된 채널 토픽(channelTopic)에 대해 redisSubscriber를 리스너로 등록
        container.addMessageListener(redisSubscriber, channelTopic);
        return container;
    }
}