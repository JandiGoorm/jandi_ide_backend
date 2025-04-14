package com.webproject.jandi_ide_backend.redis.pubsub;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChatMessageDTO message) {
        String topic = "CHAT_ROOM:" + message.getRoomId();
        log.info("Publishing to topic {}: {}", topic, message);
        redisTemplate.convertAndSend(topic, message);
    }
}