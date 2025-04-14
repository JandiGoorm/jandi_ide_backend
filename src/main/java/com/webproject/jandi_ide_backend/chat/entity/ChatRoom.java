package com.webproject.jandi_ide_backend.chat.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;      // 채팅방 고유 ID
    private String name;        // 채팅방 이름
    private String description; // 채팅방 설명
    private String createdBy;   // 생성자
    private String createdAt;   // 생성 시간

    @Builder.Default
    private Set<String> participants = new HashSet<>(); // 참여자 목록
}