package com.webproject.jandi_ide_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpTypeMatchers(
                SimpMessageType.CONNECT, 
                SimpMessageType.HEARTBEAT, 
                SimpMessageType.UNSUBSCRIBE, 
                SimpMessageType.DISCONNECT,
                SimpMessageType.MESSAGE,
                SimpMessageType.SUBSCRIBE
            ).permitAll()
            .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // CSRF 비활성화
        return true;
    }
}