package com.webproject.jandi_ide_backend.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import jakarta.annotation.PostConstruct; // Spring Boot 3+ 에서는 jakarta 사용
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Redis Pub/Sub 채널(토픽)로부터 메시지를 구독(subscribe)하는 리스너 컴포넌트입니다.
 * MessageListener 인터페이스를 구현하며, Redis에서 수신한 채팅 메시지를
 * WebSocket 클라이언트에게 STOMP를 통해 전달하는 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    // JSON 문자열 <-> Java 객체 변환을 위한 ObjectMapper (RedisConfig에서 설정됨)
    private final ObjectMapper objectMapper;
    // WebSocket 클라이언트에게 메시지를 전송하기 위한 STOMP 메시징 템플릿
    private final SimpMessageSendingOperations messagingTemplate;
    // 이 Subscriber를 Redis 리스너로 등록 및 관리하는 컨테이너 (RedisConfig에서 설정됨)
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    /**
     * 빈(Bean)이 생성되고 모든 의존성이 주입된 후 Redis 리스너 컨테이너에
     * 이 클래스(Subscriber)를 리스너로 등록합니다.
     * "CHAT_ROOM:*" 패턴에 해당하는 모든 Redis 토픽을 구독하게 됩니다.
     */
    @PostConstruct
    private void init() {
        log.info("Initializing RedisSubscriber and adding it to the listener container.");
        // "CHAT_ROOM:*" 패턴의 토픽을 구독하도록 리스너 추가
        redisMessageListenerContainer.addMessageListener(this, new PatternTopic("CHAT_ROOM:*"));
    }

    /**
     * 구독 중인 Redis 토픽에서 메시지를 수신했을 때 MessageListener 인터페이스에 의해 호출되는 메소드입니다.
     *
     * @param message 수신된 메시지 객체 (body에 실제 데이터 포함)
     * @param pattern 메시지를 수신한 토픽 패턴 (byte array)
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 1. Redis 메시지의 본문(body)을 가져와 UTF-8 문자열로 변환합니다.
            String publishMessage = new String(message.getBody(), "UTF-8"); // 인코딩 명시 권장

            // 2. 수신된 JSON 문자열을 ChatMessageDTO 객체로 역직렬화합니다.
            ChatMessageDTO chatMessage = objectMapper.readValue(publishMessage, ChatMessageDTO.class);

            // 3. 수신된 메시지 로그 출력
            log.info("Received Redis message: {}", chatMessage);

            // 4. SimpMessagingTemplate을 사용하여 해당 채팅방의 WebSocket 구독자들에게 메시지를 전송합니다.
            //    목적지(destination)는 "/topic/chat/room/{roomId}" 형식입니다.
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatMessage.getRoomId(), chatMessage);

            log.debug("Message sent to WebSocket topic: /topic/chat/room/{}", chatMessage.getRoomId());

        } catch (IOException e) {
            // JSON 파싱(역직렬화) 중 오류 발생 시 로그 기록
            log.error("Error parsing received Redis message: {}", e.getMessage(), e);
        } catch (Exception e) {
            // 그 외 예기치 못한 오류 발생 시 로그 기록
            log.error("Error processing received Redis message in onMessage: {}", e.getMessage(), e);
        }
    }
}