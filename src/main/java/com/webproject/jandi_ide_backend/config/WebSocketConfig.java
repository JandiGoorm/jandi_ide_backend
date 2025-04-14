package com.webproject.jandi_ide_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 및 STOMP 메시지 브로커 설정을 위한 구성 클래스입니다.
 *
 * @EnableWebSocketMessageBroker WebSocket 메시지 브로커 기능을 활성화합니다. STOMP 메시징을 사용할 수 있게 합니다.
 * implements WebSocketMessageBrokerConfigurer WebSocket 메시지 브로커를 구성하기 위한 메소드를 제공하는 인터페이스입니다.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 관련 설정을 구성합니다.
     * 메시지 브로커는 클라이언트 간의 메시지 라우팅, 브로드캐스팅 등을 담당합니다.
     *
     * @param registry 메시지 브로커 설정을 위한 레지스트리 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/topic"으로 시작하는 경로의 메시지를 처리하는 간단한 인메모리 메시지 브로커를 활성화합니다.
        // 클라이언트는 이 경로를 구독(subscribe)하여 메시지를 수신합니다.
        // 예: /topic/chat/room/{roomId}
        registry.enableSimpleBroker("/topic");

        // "/app"으로 시작하는 경로의 메시지를 애플리케이션의 @MessageMapping 어노테이션이 붙은 메소드로 라우팅하도록 설정합니다.
        // 클라이언트가 메시지를 서버로 보낼 때 사용하는 경로의 접두사입니다.
        // 예: 클라이언트가 /app/chat/message 로 메시지를 보내면 ChatController의 message() 메소드가 처리합니다.
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 프로토콜을 사용하기 위한 WebSocket 엔드포인트를 등록합니다.
     * 클라이언트는 이 엔드포인트를 통해 WebSocket 연결을 시작합니다.
     *
     * @param registry STOMP 엔드포인트 등록을 위한 레지스트리 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/ws/chat" 경로를 WebSocket 연결 엔드포인트로 등록합니다.
        registry.addEndpoint("/ws/chat")
                // 모든 오리진(출처)에서의 연결 요청을 허용합니다.
                // 실제 운영 환경에서는 보안을 위해 특정 도메인만 허용하도록 수정하는 것이 좋습니다. (e.g., .setAllowedOrigins("http://example.com"))
                .setAllowedOriginPatterns("*")
                // WebSocket을 지원하지 않는 브라우저 환경에서 대체 기술(e.g., HTTP Long Polling)을 사용할 수 있도록 SockJS 지원을 활성화합니다.
                .withSockJS();
    }
}