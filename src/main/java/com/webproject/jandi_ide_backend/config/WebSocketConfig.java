package com.webproject.jandi_ide_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지를 구독할 때 사용할 prefix (예: /topic)
        registry.enableSimpleBroker("/topic");
        // 서버에서 클라이언트로부터 메시지를 받을 때 사용할 prefix (예: /app)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // 웹소켓 연결 endpoint
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // SockJS 지원 활성화
    }
}