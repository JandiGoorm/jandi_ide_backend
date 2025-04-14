package com.webproject.jandi_ide_backend.chat.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    private MessageType type;

    private String roomId;

    private String sender;

    private String message;

    private String timestamp;
}