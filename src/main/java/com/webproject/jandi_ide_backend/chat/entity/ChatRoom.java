package com.webproject.jandi_ide_backend.chat.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 채팅방 정보를 나타내는 데이터 클래스(엔티티)입니다.
 * 이 객체는 Redis에 직렬화되어 저장됩니다. (현재 Jackson JSON 직렬화 사용)
 * Serializable 인터페이스를 구현하여 다양한 직렬화 방식에 대비합니다.
 * Lombok 어노테이션을 사용하여 Getter, Setter, Builder, 생성자 등을 자동으로 생성합니다.
 */
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 자동 생성
@Builder // 빌더 패턴 자동 생성
@NoArgsConstructor // 파라미터 없는 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 자동 생성
@Schema(description = "채팅방 정보")
public class ChatRoom implements Serializable {

    // 직렬화/역직렬화 시 버전 호환성을 위한 고유 ID
    private static final long serialVersionUID = 1L;

    @Schema(description = "채팅방 ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String roomId;      // 채팅방 고유 ID (UUID 사용)
    
    @Schema(description = "채팅방 이름", example = "개발팀 채팅방")
    private String name;        // 채팅방 이름
    
    @Schema(description = "채팅방 설명", example = "개발팀 일반 대화를 위한 채팅방입니다")
    private String description; // 채팅방 설명 (선택 사항)
    
    @Schema(description = "채팅방 생성자 사용자명", example = "홍길동")
    private String createdBy;   // 채팅방 생성자 사용자 이름
    
    @Schema(description = "채팅방 생성 시간 (ISO 8601 형식)", example = "2023-06-01T12:00:00")
    private String createdAt;   // 채팅방 생성 시간 (ISO 8601 형식 문자열)

    // 채팅방 참여자 목록 (사용자 이름 저장)
    // @Builder.Default: Lombok 빌더 사용 시 이 필드를 명시적으로 설정하지 않으면 빈 HashSet으로 초기화합니다.
    @Builder.Default
    @Schema(description = "채팅방 참여자 목록")
    private Set<String> participants = new HashSet<>();
}