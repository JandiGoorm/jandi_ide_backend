package com.webproject.jandi_ide_backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private String name;
    private String description;
    private String createdBy;
}