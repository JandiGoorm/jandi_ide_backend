package com.webproject.jandi_ide_backend.chat.entity;

import com.webproject.jandi_ide_backend.chat.dto.ChatMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 정보를 몽고DB에 저장하기 위한 엔티티 클래스입니다.
 * Document 어노테이션을 사용하여 MongoDB 컬렉션으로 맵핑됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;                   // MongoDB 문서 ID

    @Indexed
    private String roomId;               // 채팅방 ID (검색을 위한 인덱스)

    private ChatMessageDTO.MessageType type;  // 메시지 타입 (ENTER, TALK, LEAVE)
    private String sender;               // 발신자
    private String message;              // 메시지 내용
    private LocalDateTime timestamp;     // 전송 시간

    /**
     * ChatMessageDTO를 ChatMessage 엔티티로 변환하는 메소드
     * @param dto 변환할 DTO 객체
     * @return MongoDB에 저장할 엔티티 객체
     */
    public static ChatMessage fromDTO(ChatMessageDTO dto) {
        return ChatMessage.builder()
                .roomId(dto.getRoomId())
                .type(dto.getType())
                .sender(dto.getSender())
                .message(dto.getMessage())
                .timestamp(LocalDateTime.parse(dto.getTimestamp()))
                .build();
    }
} 