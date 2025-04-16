package com.webproject.jandi_ide_backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅방 생성 요청 시 클라이언트로부터 데이터를 받아오기 위한
 * 데이터 전송 객체(Data Transfer Object, DTO)입니다.
 * Lombok 어노테이션을 사용하여 Getter, Setter, 기본 생성자, 모든 필드 생성자 등을 자동으로 생성합니다.
 */
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 자동 생성
@NoArgsConstructor // 파라미터 없는 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 자동 생성
public class ChatRoomDTO {

    // 생성할 채팅방의 이름
    private String name;
    // 생성할 채팅방의 설명 (선택 사항)
    private String description;
    // 채팅방을 생성하는 사용자의 이름 (클라이언트 요청 본문에 포함되어 전달됨)
    private String createdBy;
}