package com.webproject.jandi_ide_backend.tech.dto;

// Lombok 라이브러리를 사용하여 Getter(읽기), Setter(쓰기) 메서드를 자동 생성
import lombok.Getter;
import lombok.Setter;

/**
 * 기술 스택(TechStack)을 생성할 때 클라이언트로부터 전달받는 요청 데이터를 담는 DTO 클래스
 * 예를 들어, 사용자가 POST /api/techstacks/add API로 기술 스택을 추가할 때,
 * JSON 요청 바디에 담긴 데이터가 이 객체로 변환됨
 * {
 *   "name": "Java"
 * }
 */
@Getter // Lombok: name 필드에 대한 Getter 메서드 자동 생성 (getName())
@Setter // Lombok: name 필드에 대한 Setter 메서드 자동 생성 (setName())
public class TechStackRequestDTO {

    /**
     * 클라이언트가 요청 바디(JSON)로 전달하는 기술 스택의 이름
     * 예시 입력:
     * {
     *   "name": "Java"
     * }
     * 이 값은 컨트롤러에서 @RequestBody를 통해 자동 매핑됨
     */
    private String name;
}
