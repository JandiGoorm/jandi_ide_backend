package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import com.webproject.jandi_ide_backend.redis.pubsub.RedisPublisher;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket STOMP 기반의 채팅 메시지를 처리하는 컨트롤러입니다.
 * 클라이언트가 특정 경로로 메시지를 발행(send)하면 해당 메시지를 수신하여 처리합니다.
 * (주의: HTTP 요청을 처리하는 @RestController가 아닙니다.)
 */
@Controller
public class ChatController {
    private final RedisPublisher redisPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public ChatController(RedisPublisher redisPublisher,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository) {
        this.redisPublisher = redisPublisher;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat/message")
    public void message(ChatMessageDTO message,
                        @Header("Authorization") String token) {
        // 토큰 검증
        if (token == null || !token.startsWith("Bearer ")) {
            throw new MessageDeliveryException("인증이 필요합니다.");
        }

        String accessToken = token.replace("Bearer ", "");
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);

        // 사용자 정보 확인
        User user = userRepository.findByGithubId(tokenInfo.getGithubId())
                .orElseThrow(() -> new MessageDeliveryException("사용자를 찾을 수 없습니다."));

        // 메시지에 사용자 정보 추가
        message.setSender(user.getNickname());
        message.setTimestamp(LocalDateTime.now().toString());

        redisPublisher.publish(message);
    }
}