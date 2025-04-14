package com.webproject.jandi_ide_backend.chat.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 채팅 메시지의 데이터 구조를 정의하는 DTO(Data Transfer Object) 클래스입니다.
 * WebSocket 통신 및 Redis 직렬화를 위해 사용됩니다.
 *
 * implements Serializable 객체 직렬화를 지원함을 명시합니다. Redis에 객체를 저장하거나 네트워크를 통해 전송할 때 필요합니다.
 */
@Data
public class ChatMessage implements Serializable {
    /**
     * 직렬화/역직렬화 시 클래스 버전 관리를 위한 고유 ID입니다.
     * 클래스 구조가 변경될 경우 이 값을 변경해야 할 수 있습니다.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 메시지의 유형을 나타내는 열거형(Enum)입니다.
     * ENTER: 사용자가 채팅방에 입장했음을 나타냅니다.
     * TALK: 일반적인 대화 메시지임을 나타냅니다.
     * LEAVE: 사용자가 채팅방에서 퇴장했음을 나타냅니다.
     */
    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    /**
     * 메시지의 유형 (입장, 대화, 퇴장)
     */
    private MessageType type;
    /**
     * 메시지가 속한 채팅방의 고유 식별자입니다.
     */
    private String roomId;
    /**
     * 메시지를 보낸 사용자의 이름 또는 식별자입니다.
     */
    private String sender;
    /**
     * 실제 채팅 메시지의 내용입니다.
     */
    private String message;
    /**
     * 메시지가 생성되거나 처리된 시간의 타임스탬프입니다. (문자열 형태)
     */
    private String timestamp;
}