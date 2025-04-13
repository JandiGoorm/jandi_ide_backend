package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessage;
import com.webproject.jandi_ide_backend.redis.pubsub.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket 채팅 메시지를 처리하는 컨트롤러 클래스입니다.
 * STOMP 프로토콜 기반의 메시지를 받아 Redis Pub/Sub 시스템으로 발행(publish)하는 역할을 담당합니다.
 *
 * @RequiredArgsConstructor Lombok 어노테이션을 사용하여 final 필드에 대한 생성자를 자동으로 생성합니다.
 * 이를 통해 RedisPublisher 의존성을 주입받습니다.
 */
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;

    /**
     * 클라이언트로부터 '/app/chat/message' 경로로 전송된 STOMP 메시지를 처리합니다.
     * (실제 경로는 WebSocketConfig에 설정된 ApplicationDestinationPrefixes에 따라 '/app'이 접두사로 붙습니다.)
     *
     * 수신된 ChatMessage 객체에 현재 서버 시간을 타임스탬프로 설정한 후,
     * RedisPublisher를 통해 해당 메시지를 Redis의 특정 토픽(채널)으로 발행합니다.
     *
     * @param message 클라이언트로부터 전송된 채팅 메시지 객체. JSON 형태의 메시지가 ChatMessage 객체로 변환되어 주입됩니다.
     * @MessageMapping("/chat/message") WebSocket 메시지 라우팅을 위한 어노테이션입니다.
     * 지정된 경로로 메시지가 도착하면 이 메소드가 호출됩니다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        // 메시지에 현재 시간 타임스탬프 설정 (ISO-8601 형식의 문자열로 변환)
        message.setTimestamp(LocalDateTime.now().toString());
        // Redis Publisher를 통해 메시지를 Redis 토픽으로 발행
        redisPublisher.publish(message);
    }
}