package com.webproject.jandi_ide_backend.chat.controller;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessage;
import com.webproject.jandi_ide_backend.chat.service.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat/message")
    public void message(@Payload ChatMessage chatMessage) {
        // 채팅방 입장 메시지일 경우 서버가 입장 메시지를 생성
        if (chatMessage.getType() == ChatMessage.MessageType.ENTER) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 입장하였습니다.");
        }

        redisPublisher.publish(ChannelTopic.of("chatroom:" + chatMessage.getRoomId()), chatMessage);
    }
}
