package com.webproject.jandi_ide_backend.chat.listner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis에서 받은 메시지를 ChatMessage 객체로 역직렬화
            String messageBody = new String(message.getBody());
            ChatMessage chatMessage = objectMapper.readValue(messageBody, ChatMessage.class);

            // /sub/chat/room/{roomId} 형식의 구독 주소로 메시지 전송
            messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getRoomId(), chatMessage);
        } catch (Exception e) {
            log.error("Redis 메시지 처리 중 에러 발생", e);
        }
    }
}
