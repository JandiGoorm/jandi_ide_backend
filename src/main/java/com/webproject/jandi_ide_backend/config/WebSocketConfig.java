package com.webproject.jandi_ide_backend.config;

import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(
                    "http://localhost:5173",
                    // 프론트엔드가 배포된 URL 추가
                    "https://your-frontend-domain.com",
                    // 개발 중인 경우 와일드카드 허용 (보안상 주의 필요)
                    "*"
                )
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    String destination = accessor.getDestination();
                    
                    log.debug("WebSocket 메시지: command={}, destination={}", command, destination);
                    
                    // CONNECT 명령이거나 메시지 송신인 경우 인증 검사
                    if (StompCommand.CONNECT.equals(command) || 
                        StompCommand.SEND.equals(command) || 
                        (StompCommand.SUBSCRIBE.equals(command) && destination != null && destination.startsWith("/topic/chat"))) {
                        
                        // 인증 헤더에서 토큰 추출
                        List<String> authHeaders = accessor.getNativeHeader("Authorization");
                        if (authHeaders != null && !authHeaders.isEmpty()) {
                            String authHeader = authHeaders.get(0);
                            
                            if (authHeader.startsWith("Bearer ")) {
                                String token = authHeader.substring(7);
                                
                                try {
                                    if (jwtTokenProvider.validateToken(token)) {
                                        Authentication auth = jwtTokenProvider.getAuthentication(token);
                                        SecurityContextHolder.getContext().setAuthentication(auth);
                                        accessor.setUser(auth);
                                        log.debug("JWT 인증 성공: {}, 커맨드: {}, 목적지: {}", 
                                                auth.getName(), command, destination);
                                        
                                        // 토큰 만료 임박 로깅 (디버깅용)
                                        long expirationSeconds = jwtTokenProvider.getTokenExpirationInSeconds(token);
                                        if (expirationSeconds < 300) { // 5분 이내 만료 예정
                                            log.info("토큰이 곧 만료됩니다 ({}초 남음)", expirationSeconds);
                                        }
                                    } else {
                                        log.warn("유효하지 않은 JWT 토큰. 커맨드: {}, 목적지: {}", command, destination);
                                        // 인증 실패 처리 - CONNECT 명령의 경우 연결 거부
                                        if (StompCommand.CONNECT.equals(command)) {
                                            throw new IllegalArgumentException("유효하지 않은 인증 토큰");
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("JWT 토큰 처리 중 오류: {}, 커맨드: {}, 목적지: {}", 
                                            e.getMessage(), command, destination);
                                    
                                    // CONNECT가 아닌 명령의 경우 오류를 로깅하고 처리 계속
                                    if (!StompCommand.CONNECT.equals(command)) {
                                        // 기본 권한 설정 (USER)
                                        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                "guest", "", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_GUEST")));
                                        accessor.setUser(auth);
                                        log.debug("토큰 오류, 게스트 권한 할당: 커맨드: {}, 목적지: {}", command, destination);
                                    } else {
                                        // CONNECT 명령의 경우에는 예외를 발생시켜 연결 거부
                                        throw new IllegalArgumentException("인증 처리 중 오류 발생: " + e.getMessage());
                                    }
                                }
                            } else {
                                log.warn("잘못된 인증 헤더 형식: Bearer 형식이 아님. 커맨드: {}, 목적지: {}", 
                                        command, destination);
                                if (StompCommand.CONNECT.equals(command)) {
                                    throw new IllegalArgumentException("인증 헤더 형식이 잘못되었습니다");
                                }
                            }
                        } else {
                            log.warn("인증 헤더 없음. 커맨드: {}, 목적지: {}", command, destination);
                            // CONNECT 명령의 경우 인증 헤더가 없으면 연결 거부
                            if (StompCommand.CONNECT.equals(command)) {
                                throw new IllegalArgumentException("인증 헤더가 없습니다");
                            }
                        }
                    }
                    
                    // 연결이 끊어질 때 로그 추가
                    if (StompCommand.DISCONNECT.equals(command)) {
                        log.info("WebSocket 연결 종료: {}", 
                                accessor.getUser() != null ? accessor.getUser().getName() : "알 수 없는 사용자");
                    }
                }
                
                return message;
            }
        });
    }
}