package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import com.webproject.jandi_ide_backend.redis.pubsub.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller; // Spring MVC의 @Controller 사용

import java.time.LocalDateTime;

/**
 * WebSocket STOMP 기반의 채팅 메시지를 처리하는 컨트롤러입니다.
 * 클라이언트가 특정 경로로 메시지를 발행(send)하면 해당 메시지를 수신하여 처리합니다.
 * (주의: HTTP 요청을 처리하는 @RestController가 아닙니다.)
 */
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (Lombok)
@Controller // 이 클래스가 메시지 처리 등의 역할을 하는 컨트롤러임을 나타냅니다.
public class ChatController {

    // Redis Pub/Sub 토픽으로 메시지를 발행하는 컴포넌트 (의존성 주입)
    private final RedisPublisher redisPublisher;

    /**
     * 클라이언트가 WebSocket을 통해 "/app/chat/message" 목적지로 메시지를 전송할 때 호출되는 메소드입니다.
     * STOMP 메시지의 페이로드(본문)가 ChatMessageDTO로 매핑됩니다.
     *
     * @param message 클라이언트로부터 수신된 채팅 메시지 데이터 객체
     */
    @MessageMapping("/chat/message") // "/app" 접두사가 붙은 "/app/chat/message" 경로로 오는 메시지를 처리합니다.
    public void message(ChatMessageDTO message) {
        // 1. 수신된 메시지에 서버의 현재 시간을 타임스탬프로 설정합니다.
        //    (클라이언트 시간 대신 서버 시간을 기준으로 일관성 유지)
        message.setTimestamp(LocalDateTime.now().toString());

        // 2. RedisPublisher를 사용하여 메시지를 해당 Redis 토픽으로 발행합니다.
        //    이후 RedisSubscriber가 이 메시지를 수신하여 실제 WebSocket 클라이언트들에게 브로드캐스트합니다.
        redisPublisher.publish(message);

        // 별도의 반환 값은 없습니다. 메시지 발행 후 로직이 종료됩니다.
        // @SendTo 어노테이션을 사용하면 이 메소드의 반환 값을 특정 토픽으로 보낼 수도 있습니다.
    }
}