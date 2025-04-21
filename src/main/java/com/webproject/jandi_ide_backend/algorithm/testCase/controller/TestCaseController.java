package com.webproject.jandi_ide_backend.algorithm.testCase.controller;

import com.webproject.jandi_ide_backend.algorithm.testCase.dto.TestCaseRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.testCase.dto.TestCaseResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.testCase.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="TestCase",description = "문제의 테스트 케이스 관련 API")
@RestController
@RequestMapping("/api/test-cases")
public class TestCaseController {
    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @PutMapping("/{id}")
    @Operation(summary = "테스트 케이스 수정 (STAFF 이상)",description = "테스트 케이스를 수정합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TestCaseResponseDTO.class))
            ),
    })
    public ResponseEntity<TestCaseResponseDTO> updateTestCase(
            @RequestBody TestCaseRequestDTO requestDTO,
            @PathVariable Integer id
    ){
        TestCaseResponseDTO testCaseResponseDTO = testCaseService.updateTestCase(requestDTO, id);
        return ResponseEntity.ok(testCaseResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "테스트 케이스 삭제 (STAFF 이상)",description = "테스트 케이스를 삭제합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "테스트 케이스 삭제 성공")
                    )
            )
    })
    public ResponseEntity<String>deleteTestCase(@PathVariable Integer id){
        testCaseService.deleteTestCase(id);
        return ResponseEntity.ok("테스트 케이스 삭제 성공");
    }
}
