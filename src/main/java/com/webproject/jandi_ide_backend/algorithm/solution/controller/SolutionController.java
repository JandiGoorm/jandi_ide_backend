package com.webproject.jandi_ide_backend.algorithm.solution.controller;

import com.webproject.jandi_ide_backend.algorithm.solution.dto.ProblemSetSolutionsDto;
import com.webproject.jandi_ide_backend.algorithm.solution.dto.SolutionResponseDto;
import com.webproject.jandi_ide_backend.algorithm.solution.service.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solutions")
@Tag(name = "솔루션 API", description = "문제 풀이(솔루션) 관련 API")
public class SolutionController {

    private final SolutionService solutionService;

    public SolutionController(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    @GetMapping("/successful/{problemId}")
    @Operation(
        summary = "성공한 풀이 목록 조회",
        description = "특정 문제에 대해 성공한 풀이 목록을 조회합니다."
    )
    public ResponseEntity<List<SolutionResponseDto>> getSuccessfulSolutions(
            @Parameter(description = "문제 ID", required = true)
            @PathVariable Integer problemId) {
        List<SolutionResponseDto> solutions = solutionService.findSuccessfulSolutions(problemId);
        return ResponseEntity.ok(solutions);
    }

    @GetMapping("/user/{userId}/problem-set/{problemSetId}")
    @Operation(
        summary = "사용자의 문제집 풀이 조회",
        description = "특정 사용자가 특정 문제집에 대해 제출한 풀이 정보와 문제 정보를 조회합니다."
    )
    public ResponseEntity<ProblemSetSolutionsDto> getUserProblemSetSolutions(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "문제집 ID", required = true)
            @PathVariable Long problemSetId) {
        ProblemSetSolutionsDto result = solutionService.findUserProblemSetSolutions(userId, problemSetId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}/problem/{problemId}")
    @Operation(
        summary = "사용자의 특정 문제 풀이 목록 조회",
        description = "특정 사용자가 특정 문제에 대해 제출한 모든 풀이 목록을 조회합니다."
    )
    public ResponseEntity<List<SolutionResponseDto>> getUserSolutionsForProblem(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "문제 ID", required = true)
            @PathVariable Integer problemId) {
        List<SolutionResponseDto> solutions = solutionService.findUserSolutionsForProblem(userId, problemId);
        return ResponseEntity.ok(solutions);
    }
} 