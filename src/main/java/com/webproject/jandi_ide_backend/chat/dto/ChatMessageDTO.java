package com.webproject.jandi_ide_backend.chat.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 채팅 메시지 정보를 담는 데이터 전송 객체(DTO)입니다.
 * WebSocket을 통해 클라이언트와 서버 간에 메시지를 주고받거나,
 * Redis Pub/Sub을 통해 메시지를 발행/구독할 때 사용됩니다.
 * Serializable 인터페이스를 구현하며, Lombok의 @Data 어노테이션을 사용합니다.
 */
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 자동 생성
public class ChatMessageDTO implements Serializable {

    // 직렬화/역직렬화 시 버전 호환성을 위한 고유 ID
    private static final long serialVersionUID = 1L;

    /**
     * 메시지의 유형을 정의하는 열거형 (Enum) 입니다.
     * 메시지의 성격(입장, 대화, 퇴장)을 구분하는 데 사용됩니다.
     */
    public enum MessageType {
        ENTER, // 사용자가 채팅방에 입장했음을 알리는 타입
        TALK,  // 일반적인 사용자 간 대화 메시지 타입
        LEAVE  // 사용자가 채팅방에서 퇴장했음을 알리는 타입
    }

    // 메시지 타입 (MessageType 열거형 값: ENTER, TALK, LEAVE)
    private MessageType type;

    // 메시지가 전송된 채팅방의 고유 ID
    private String roomId;

    // 메시지를 보낸 사용자의 이름 (닉네임)
    private String sender;

    // 실제 메시지 내용 (일반 대화(TALK) 시 사용됨)
    private String message;

    // 메시지가 생성되거나 서버에 도달한 시간 (ISO-8601 형식의 문자열 권장)
    private String timestamp;
}