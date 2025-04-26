package com.webproject.jandi_ide_backend.algorithm.problemSet.controller;

import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.*;
import com.webproject.jandi_ide_backend.algorithm.problemSet.service.ProblemSetService;
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

@Tag(name="ProblemSet",description = "문제집 관련 API")
@RestController
@RequestMapping("/api/problem-set")
public class ProblemSetController {
    private final ProblemSetService problemSetService;

    public ProblemSetController(ProblemSetService problemSetService) {
        this.problemSetService = problemSetService;
    }

    /// create
    @PostMapping
    @Operation(summary = "문제집을 생성",
            description = "문제집을 생성합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = RespProblemSetDTO.class)
                    )
            )
    })
    public RespProblemSetDTO createProblemSet(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestBody ReqPostProblemSetDTO probSetDTO
    ) {
        String githubId = problemSetService.getGithubIdFromToken(token);
        return problemSetService.createProblemSet(probSetDTO, githubId);
    }

    /// read
    // 전체 문제집 조회
    @GetMapping
    @Operation(summary = "문제집 목록 조회",
            description = "사용자의 문제집 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = RespProblemSetPageDTO.class))
                    )
            )
    })
    public RespProblemSetPageDTO readProblemSet(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestParam(value="page",defaultValue = "0") Integer page,
            @RequestParam(value="size",defaultValue = "10") Integer size
    ) {
        String githubId = problemSetService.getGithubIdFromToken(token);
        return problemSetService.readProblemSet(githubId,page,size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 문제집 조회",
            description = "특정 문제집 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = RespDetailProblemSet.class)
                    )
            )
    })
    public RespDetailProblemSet readProblemSetDetail(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ){
        return problemSetService.readProblemSetDetail(id);
    }

    /// update
    @PutMapping("{problemSetId}")
    @Operation(summary = "문제집 수정",
            description = "특정 문제집을 수정합니다.",
            security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = RespProblemSetDTO.class)
                    )
            )
    })
    public RespProblemSetDTO updateProblemSet(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Long problemSetId,
            @RequestBody ReqUpdateProblemSetDTO probSetDTO
            ) {
        String githubId = problemSetService.getGithubIdFromToken(token);
        return problemSetService.updateProblemSet(problemSetId, probSetDTO, githubId);
    }

    /// delete
    @DeleteMapping("{problemSetId}")
    @Operation(summary = "문제집 삭제",
            description = "특정 문제집을 삭제합니다.",
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
    public ResponseEntity<String> deleteProblemSet(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Long problemSetId
    ) {
        String githubId = problemSetService.getGithubIdFromToken(token);
        if (!problemSetService.deleteProblemSet(problemSetId, githubId))
            throw new RuntimeException("알수없는 이유로 삭제에 실패했습니다. 다시 시도해주세요");
        return ResponseEntity.ok("삭제되었습니다");
    }
}
