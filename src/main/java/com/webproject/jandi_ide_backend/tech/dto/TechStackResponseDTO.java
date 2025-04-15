package com.webproject.jandi_ide_backend.tech.dto;

// Lombok 라이브러리를 사용하여 Getter(읽기) 메서드와 생성자를 자동 생성
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 클라이언트에게 응답할 때 사용하는 DTO 클래스
 * 기술 스택(TechStack)을 성공적으로 추가하거나 조회했을 때,
 * 클라이언트에게 반환되는 데이터 구조를 정의함.
 * 예시 응답:
 * {
 *   "id": 1,
 *   "name": "Java"
 * }
 */
@Getter // 각 필드에 대한 Getter 메서드를 자동 생성 (getId(), getName())
@AllArgsConstructor // 모든 필드를 초기화하는 생성자를 자동 생성 (생성자 주입에 사용)
public class TechStackResponseDTO {

    /**
     * 데이터베이스에서 자동 생성된 기술 스택의 고유 ID
     * 예: 1, 2, 3, ...
     */
    private Integer id;

    /**
     * 기술 스택의 이름
     * 예: "Java", "Spring", "Python"
     */
    private String name;
}
