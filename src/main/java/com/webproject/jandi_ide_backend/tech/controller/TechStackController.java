package com.webproject.jandi_ide_backend.tech.controller;

// DTO: 클라이언트에서 넘어온 요청 데이터를 담기 위한 객체
import com.webproject.jandi_ide_backend.tech.dto.TechStackRequestDTO;
// DTO: 클라이언트로 응답할 데이터를 담기 위한 객체
import com.webproject.jandi_ide_backend.tech.dto.TechStackResponseDTO;
// 비즈니스 로직을 처리하는 서비스 클래스
import com.webproject.jandi_ide_backend.tech.service.TechStackService;

// Spring에서 제공하는 HTTP 응답 객체
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
     */
    @PostMapping("/add")
    public ResponseEntity<TechStackResponseDTO> addTechStack(@RequestBody TechStackRequestDTO requestDto) {
        // 서비스 레이어에 요청 DTO를 전달하여 저장(기술스택 생성) 로직을 수행함
        TechStackResponseDTO responseDto = techStackService.createTechStack(requestDto);

        // HTTP 상태코드 200(OK)과 함께 저장된 기술 스택 정보를 JSON 형태로 응답
        return ResponseEntity.ok(responseDto);
    }
}
