package com.webproject.jandi_ide_backend.chat.dto;

import com.webproject.jandi_ide_backend.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채팅 메시지의 DTO(Data Transfer Object) 클래스입니다.
 * 클라이언트와 서버 간의 채팅 메시지 데이터 전송에 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    /**
     * 메시지 타입을 정의하는 열거형입니다.
     * ENTER: 채팅방 입장 메시지
     * TALK: 일반 대화 메시지
     * LEAVE: 채팅방 퇴장 메시지
     */
    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    // 메시지 타입
    private MessageType type;

    // 메시지가 속한 채팅방 ID
    private String roomId;

    // 메시지 발신자
    private String sender;

    // 메시지 내용
    private String message;

    // 메시지 생성 시간 (ISO-8601 형식의 문자열)
    private String timestamp;
    
    // 메시지를 보낸 사용자의 프로필 이미지 URL
    private String profileImage;

    /**
     * ChatMessage 엔티티로부터 DTO 객체를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param entity ChatMessage 엔티티
     * @return 생성된 ChatMessageDTO 객체
     */
    public static ChatMessageDTO from(ChatMessage entity) {
        return ChatMessageDTO.builder()
                .type(entity.getType())
                .roomId(entity.getRoomId())
                .sender(entity.getSender())
                .message(entity.getMessage())
                .timestamp(entity.getTimestamp().toString())
                .profileImage(entity.getProfileImage())
                .build();
    }
}