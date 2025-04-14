package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import com.webproject.jandi_ide_backend.redis.pubsub.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;

    @MessageMapping("/chat/message")
    public void message(ChatMessageDTO message) {
        message.setTimestamp(LocalDateTime.now().toString());
        redisPublisher.publish(message);
    }
}