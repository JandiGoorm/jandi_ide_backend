package com.webproject.jandi_ide_backend.algorithm.problem.controller;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemDetailResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemPageResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.service.ProblemService;
import com.webproject.jandi_ide_backend.algorithm.testCase.dto.TestCaseRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.testCase.dto.TestCaseResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.testCase.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="Problem", description = "문제 관련 API")
@RestController
@RequestMapping("/api/problems")
public class ProblemController {
    private final ProblemService problemService;
    private final TestCaseService testCaseService;

    public ProblemController(ProblemService problemService, TestCaseService testCaseService) {
        this.problemService = problemService;
        this.testCaseService = testCaseService;
    }

    @GetMapping
    @Operation(summary = "전체 문제 조회",
            description = "모든 문제의 목록을 조회합니다. 정렬 옵션 추가: sort=level&direction=asc/desc로 난이도 정렬 가능",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ProblemPageResponseDTO.class))
                    )
            )
    })
    public ResponseEntity<ProblemPageResponseDTO> findAllProblems(
            @RequestParam(value="page",defaultValue = "0") Integer page,
            @RequestParam(value="size",defaultValue = "10") Integer size,
            @Parameter(description = "정렬 기준 (level: 난이도순 정렬)") @RequestParam(value="sort", required = false) String sort,
            @Parameter(description = "정렬 방향 (asc: 오름차순, desc: 내림차순)") @RequestParam(value="direction", required = false) String direction
    ){
        ProblemPageResponseDTO problems = problemService.getProblems(page, size, sort, direction);
        return ResponseEntity.ok(problems);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 문제 조회 (테스트 케이스 포함)",
            description = "특정 문제를 조회합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetailResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ProblemDetailResponseDTO> getProblemDetail(
            @PathVariable Integer id
    ){
        ProblemDetailResponseDTO problemDetailResponseDTO = problemService.getProblemDetail(id);
        return ResponseEntity.ok(problemDetailResponseDTO);
    }

    @PostMapping
    @Operation(summary = "문제 추가 (STAFF 이상)",
            description = "문제를 추가합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "추가 성공",
                    content = @Content(
                            schema = @Schema(implementation = ProblemResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ProblemResponseDTO> postProblem(
            @RequestBody ProblemRequestDTO requestDTO
    ){
        ProblemResponseDTO problemResponseDTO = problemService.postProblem(requestDTO);
        return ResponseEntity.ok(problemResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "문제 수정 (STAFF 이상)",
            description = "특정 문제를 수정합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = ProblemResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ProblemResponseDTO> updateProblem(
            @Parameter(description = "문제 ID", example = "1") @PathVariable Integer id,
            @RequestBody ProblemRequestDTO requestDTO
    ){
        ProblemResponseDTO problem = problemService.updateProblem(requestDTO,id);
        return ResponseEntity.ok(problem);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "문제 삭제 (STAFF 이상)",
            description = "특정 문제를 삭제합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            schema = @Schema(implementation = String.class)
                    )
            )
    })
    public ResponseEntity<String> deleteProblem(
            @Parameter(description = "문제 ID", example = "1") @PathVariable Integer id
    ){
        problemService.deleteProblem(id);
        return ResponseEntity.ok("문제 삭제 성공");
    }

    @PostMapping("/{id}/test-case")
    @Operation(summary = "테스트 케이스 추가 (STAFF 이상)",
            description = "해당 문제의 테스트케이스를 추가합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "추가 성공",
                    content = @Content(
                            schema = @Schema(implementation = TestCaseResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<TestCaseResponseDTO> postTestCase(
            @RequestBody TestCaseRequestDTO requestDTO,
            @PathVariable Integer id
    ){
        TestCaseResponseDTO testCaseResponseDTO = testCaseService.postTestCase(requestDTO,id);
        return ResponseEntity.ok(testCaseResponseDTO);
    }
}
