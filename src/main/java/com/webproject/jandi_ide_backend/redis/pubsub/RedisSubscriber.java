package com.webproject.jandi_ide_backend.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Redis Pub/Sub 시스템에서 메시지를 구독(subscribe)하여 처리하는 리스너 컴포넌트입니다.
 * RedisMessageListenerContainer에 의해 특정 채널(토픽)로부터 메시지를 수신하면,
 * 이를 처리하여 WebSocket 클라이언트에게 전달하는 역할을 합니다.
 *
 * @RequiredArgsConstructor Lombok 어노테이션을 사용하여 final 필드에 대한 생성자를 자동으로 생성합니다.
 * 이를 통해 ObjectMapper와 SimpMessagingTemplate 의존성을 주입받습니다.
 * implements MessageListener Spring Data Redis에서 제공하는 인터페이스로, Redis 메시지 수신 시 호출될 메소드(onMessage)를 정의해야 합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RedisSubscriber implements MessageListener {

    /**
     * JSON 데이터와 Java 객체 간의 변환을 담당하는 Jackson ObjectMapper입니다.
     * RedisConfig에서 커스터마이징된 빈이 주입됩니다.
     */
    private final ObjectMapper objectMapper;
    /**
     * WebSocket STOMP 메시지를 특정 목적지(destination)로 전송하기 위한 Spring의 템플릿입니다.
     * 주로 메시지 브로커(/topic 등)를 통해 연결된 클라이언트에게 메시지를 보낼 때 사용됩니다.
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Redis 채널(토픽)로부터 메시지가 수신되었을 때 RedisMessageListenerContainer에 의해 호출되는 메소드입니다.
     *
     * @param message Redis로부터 수신된 원시 메시지 객체. 메시지 본문(body)과 채널 정보 등을 포함합니다.
     * @param pattern 메시지를 수신한 채널의 패턴 (이 예제에서는 사용되지 않음).
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis 메시지 본문(byte[])을 UTF-8 문자열로 변환합니다.
            // Redis에 저장될 때 ChatMessage 객체가 JSON 문자열로 직렬화되었으므로, 여기서도 문자열로 받습니다.
            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("Redis로부터 수신된 메시지: {}", publishMessage);

            // 수신된 JSON 문자열 메시지를 ChatMessage 객체로 역직렬화합니다.
            ChatMessage chatMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
            log.info("WebSocket으로 전송할 메시지 (/topic/chat/room/{}): {}", chatMessage.getRoomId(), chatMessage);

            // SimpMessagingTemplate을 사용하여 역직렬화된 ChatMessage 객체를
            // 해당 채팅방 ID에 맞는 WebSocket 토픽 경로로 전송합니다.
            // 경로는 "/topic/chat/room/{roomId}" 형태가 됩니다.
            // 이 토픽을 구독하고 있는 클라이언트들에게 메시지가 브로드캐스트됩니다.
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatMessage.getRoomId(), chatMessage);
        } catch (IOException e) {
            // JSON 파싱 중 오류 발생 시 로그를 기록합니다.
            log.error("채팅 메시지 파싱 오류: {}", e.getMessage(), e);
        }
    }
}