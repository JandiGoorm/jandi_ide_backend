package com.webproject.jandi_ide_backend.common.dto;

// Lombok 라이브러리에서 제공하는 어노테이션으로,
// 모든 필드에 대한 Getter 메서드를 자동 생성
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ✅ 공통 응답 DTO 클래스
 * API 요청에 대한 응답을 일정한 형식으로 전달하기 위한 객체입니다.
 * 모든 응답에 대해 상태 코드(code)와 메시지(message)를 통일된 구조로 반환하여
 * 클라이언트가 처리하기 쉽게 해줍니다.
 * 📌 사용 예시:
 * {
 *   "code": 200,
 *   "message": "요청이 정상적으로 처리되었습니다."
 * }
 * ✔️ 장점:
 * - 모든 API 응답 포맷을 일관되게 유지할 수 있음
 * - 클라이언트가 code와 message 기반으로 UI를 구성하거나 예외 처리하기 쉬움
 */
@Getter // 각 필드에 대해 Getter 메서드 생성 (getCode(), getMessage())
@AllArgsConstructor // 모든 필드를 초기화하는 생성자 자동 생성
public class CommonResponseDTO {

    /**
     * 🔹 응답 코드
     * - 일반적으로 HTTP 상태 코드 (예: 200, 404, 500 등)
     * - 서버가 실제로 응답한 상태코드를 그대로 담아 클라이언트가 쉽게 확인 가능
     */
    private int code;

    /**
     * 🔹 메시지
     * - 사용자에게 전달할 안내 메시지
     * - 성공 메시지, 에러 메시지 등 다양하게 사용됨
     * - 프론트엔드 UI 표시용으로 자주 활용
     */
    private String message;
}
