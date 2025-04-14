package com.webproject.jandi_ide_backend.redis.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 시스템에 메시지를 발행(publish)하는 역할을 담당하는 컴포넌트입니다.
 * 특정 채널 토픽으로 메시지를 전송합니다.
 *
 * @RequiredArgsConstructor Lombok 어노테이션을 사용하여 final 필드에 대한 생성자를 자동으로 생성합니다.
 * 이를 통해 RedisTemplate과 ChannelTopic 의존성을 주입받습니다.
 */
@RequiredArgsConstructor
@Component
public class RedisPublisher {

    /**
     * Redis 데이터 조작 및 메시지 발행에 사용되는 RedisTemplate입니다.
     * RedisConfig에서 설정된 직렬화 방식을 사용합니다.
     */
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 메시지를 발행할 Redis 채널(토픽) 정보입니다.
     * RedisConfig에서 정의된 "chatroom" 토픽 빈이 주입됩니다.
     */
    private final ChannelTopic topic;

    /**
     * 주어진 메시지 객체를 Redis의 지정된 토픽으로 발행(publish)합니다.
     * RedisTemplate은 메시지 객체를 RedisConfig에서 설정된 직렬화 방식(JSON)으로 변환하여 전송합니다.
     *
     * @param message 발행할 메시지 객체 (예: ChatMessage 인스턴스)
     */
    public void publish(Object message) {
        // redisTemplate의 convertAndSend 메소드를 사용하여 지정된 토픽(topic.getTopic())으로 메시지를 발행합니다.
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}