package com.webproject.jandi_ide_backend.algorithm.problem.controller;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.service.ProblemService;
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

import java.util.List;

@Tag(name="Problem", description = "문제 관련 API")
@RestController
@RequestMapping("/api/problems")
public class ProblemController {
    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    @Operation(summary = "전체 문제 조회",
            description = "모든 문제의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ProblemResponseDTO.class))
                    )
            )
    })
    public ResponseEntity<List<ProblemResponseDTO>> findAllProblems(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token
    ){
        List<ProblemResponseDTO> problems = problemService.getProblems();
        return ResponseEntity.ok(problems);
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
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
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
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @Parameter(description = "문제 ID", example = "1") @PathVariable Integer id
    ){
        problemService.deleteProblem(id);
        return ResponseEntity.ok("문제 삭제 성공");
    }
}
