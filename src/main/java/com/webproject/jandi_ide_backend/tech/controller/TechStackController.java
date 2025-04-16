package com.webproject.jandi_ide_backend.tech.controller;

// DTO: 클라이언트에서 넘어온 요청 데이터를 담기 위한 객체
import com.webproject.jandi_ide_backend.common.dto.CommonResponseDTO;
import com.webproject.jandi_ide_backend.tech.dto.TechStackRequestDTO;
// DTO: 클라이언트로 응답할 데이터를 담기 위한 객체
import com.webproject.jandi_ide_backend.tech.dto.TechStackResponseDTO;
// 비즈니스 로직을 처리하는 서비스 클래스
import com.webproject.jandi_ide_backend.tech.service.TechStackService;

// Spring에서 제공하는 HTTP 응답 객체
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// REST API 컨트롤러로 지정하는 애너테이션 (자동 JSON 변환 포함)
import org.springframework.web.bind.annotation.*;

/**
 * 기술 스택(TechStack)에 대한 REST API 요청을 처리하는 컨트롤러 클래스
 */
@RestController
@RequestMapping("/api/techstacks") // 이 컨트롤러 내 모든 요청은 /api/techstacks 경로를 기준으로 처리됨
public class TechStackController {

    // 서비스 레이어 객체를 주입받기 위한 필드
    private final TechStackService techStackService;

    /**
     * 생성자 주입 방식으로 TechStackService 객체를 주입받음
     * 스프링이 자동으로 의존성을 주입해줌
     */
    public TechStackController(TechStackService techStackService) {
        this.techStackService = techStackService;
    }

    /**
     * 기술 스택을 추가하는 POST 요청을 처리하는 메서드
     * 경로: POST /api/techstacks/add
     *
     * @param requestDto 클라이언트에서 전달한 기술 스택 이름 정보
     * @return 추가된 기술 스택 정보를 담은 응답 객체
     * 예시 요청:
     * {
     *   "name": "Java"
     * }
     */
    @PostMapping("/add")
    public ResponseEntity<TechStackResponseDTO> addTechStack(@RequestBody TechStackRequestDTO requestDto) {
        // 서비스 레이어에 요청 DTO를 전달하여 저장(기술스택 생성) 로직을 수행함
        TechStackResponseDTO responseDto = techStackService.createTechStack(requestDto);

        // HTTP 상태코드 200(OK)과 함께 저장된 기술 스택 정보를 JSON 형태로 응답
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 기존 기술 스택의 이름을 수정하는 API
     * HTTP 메서드: PUT
     * 엔드포인트: /api/techstacks/update/{id}
     *
     * @param id 수정하려는 기술 스택의 고유 ID (경로 변수)
     * @param requestDto 클라이언트가 보낸 JSON 요청 데이터 (새로운 기술 스택 이름)
     * @return 수정된 기술 스택의 ID와 이름을 담은 응답 객체 (HTTP 200 OK)
     * 예시 요청:
     * PUT /api/techstacks/update/3
     * {
     *   "name": "Spring Boot"
     * }
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<TechStackResponseDTO> updateTechStack(
            @PathVariable Integer id, // URL 경로에서 ID 값 추출
            @RequestBody TechStackRequestDTO requestDto // 요청 본문에서 새로운 name 추출
    ) {
        // 서비스에서 기술 스택 수정 로직 처리 후 결과 반환
        TechStackResponseDTO responseDto = techStackService.updateTechStack(id, requestDto);

        // 결과를 HTTP 응답 본문으로 반환 (상태 코드: 200 OK)
        return ResponseEntity.ok(responseDto);
    }


    /**
     * 기술 스택을 삭제하는 API
     * - HTTP 메서드: DELETE
     * - 경로: /api/techstacks/delete/{id}
     * ID를 기준으로 해당 기술 스택을 삭제하고, 공통 응답 포맷으로 성공 또는 실패 메시지를 반환합니다.
     * @param id 삭제할 기술 스택의 고유 식별자
     * @return 성공 또는 실패 메시지와 HTTP 상태코드가 포함된 JSON 응답
     * ✅ 예시 요청:
     * DELETE /api/techstacks/delete/5
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CommonResponseDTO> deleteTechStack(@PathVariable Integer id) {
        try {
            // 비즈니스 로직 실행: ID로 삭제 시도
            techStackService.deleteTechStack(id);

            // 삭제 성공 시: HTTP 200 OK + 성공 메시지
            HttpStatus status = HttpStatus.OK;
            return new ResponseEntity<>(
                    new CommonResponseDTO(status.value(), "기술 스택이 삭제되었습니다."),
                    status
            );

        } catch (IllegalArgumentException ei) {
            // 존재하지 않는 ID 등으로 삭제 실패 시: HTTP 404 Not Found + 예외 메시지
            HttpStatus status = HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(
                    new CommonResponseDTO(status.value(), ei.getMessage()),
                    status
            );

        } catch (Exception e) {
            // 서버 내부 문제 등 예외 발생 시: HTTP 500 Internal Server Error
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return new ResponseEntity<>(
                    new CommonResponseDTO(status.value(), e.getMessage()),
                    status
            );
        }
    }

}
