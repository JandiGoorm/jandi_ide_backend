package com.webproject.jandi_ide_backend.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    // 빈이 생성되고 의존성 주입이 완료된 후 실행될 메서드
    @PostConstruct
    public void init() {
        log.info("Adding RedisSubscriber as message listener.");
        // 여기서 리스너 등록
        redisMessageListenerContainer.addMessageListener(this, new PatternTopic("CHAT_ROOM:*"));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = new String(message.getBody());
            ChatMessageDTO chatMessage = objectMapper.readValue(publishMessage, ChatMessageDTO.class);

            log.info("Received message: {}", chatMessage);
            messagingTemplate.convertAndSend(
                    "/topic/chat/room/" + chatMessage.getRoomId(), // Send to WebSocket topic
                    chatMessage
            );
        } catch (IOException e) {
            log.error("Error parsing chat message: ", e);
        }
    }
}