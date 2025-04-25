package com.webproject.jandi_ide_backend.tech.controller;

import com.webproject.jandi_ide_backend.tech.dto.RespTechStackDTO;
import com.webproject.jandi_ide_backend.tech.service.TechStackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tech-stack")
@Tag(name = "기술 스택 API", description = "기술 스택 정보 제공을 위한 API")
public class TechStackController {
    private final TechStackService techStackService;

    @GetMapping("")
    @Operation(
        summary = "전체 기술 스택 조회",
        description = "시스템에 등록된 모든 기술 스택 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "기술 스택 목록 조회 성공")
    })
    public List<RespTechStackDTO> getTechStack() {
        return techStackService.getTechStack();
    }
}
