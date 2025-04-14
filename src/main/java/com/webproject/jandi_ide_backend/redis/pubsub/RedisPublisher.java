package com.webproject.jandi_ide_backend.redis.pubsub;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 토픽에 메시지를 발행(publish)하는 컴포넌트입니다.
 * 채팅 메시지를 받아 적절한 Redis 채널로 전송하는 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

    // Redis 작업을 위한 RedisTemplate 주입 (RedisConfig에서 설정됨)
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 지정된 채팅 메시지(ChatMessageDTO)를 해당 채팅방 ID에 맞는 Redis 토픽으로 발행합니다.
     *
     * @param message 발행할 채팅 메시지 객체
     */
    public void publish(ChatMessageDTO message) {
        // 메시지를 발행할 토픽 이름을 동적으로 생성 (예: "CHAT_ROOM:roomId123")
        String topic = "CHAT_ROOM:" + message.getRoomId();

        // 발행 로그 출력 (어떤 토픽에 어떤 메시지가 발행되는지 확인)
        log.info("Publishing to topic {}: {}", topic, message);

        // RedisTemplate의 convertAndSend 메소드를 사용하여 메시지 발행
        // 설정된 직렬화 방식(RedisConfig의 Jackson2JsonRedisSerializer)에 따라
        // message 객체가 JSON 등으로 변환되어 전송됩니다.
        redisTemplate.convertAndSend(topic, message);
    }
}