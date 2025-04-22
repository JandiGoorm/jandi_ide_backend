package com.webproject.jandi_ide_backend.algorithm.problemSet.controller;

import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.ReqPostProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.ReqUpdateProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.RespProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.RespSpecProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.service.ProblemSetService;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
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

@Tag(name="ProblemSet",description = "문제집 관련 API")
@RestController
@RequestMapping("/api/problem-set")
public class ProblemSetController {
    private final ProblemSetService problemSetService;
    private final JwtTokenProvider jwtTokenProvider;

    public ProblemSetController(ProblemSetService problemSetService, JwtTokenProvider jwtTokenProvider) {
        this.problemSetService = problemSetService;
        this.jwtTokenProvider = jwtTokenProvider;
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
        String githubId = getGithubIdFromToken(token);
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
                            array = @ArraySchema(schema = @Schema(implementation = RespProblemSetDTO.class))
                    )
            )
    })
    public List<RespProblemSetDTO> readProblemSet(
            @RequestHeader("Authorization") String token
    ) {
        String githubId = getGithubIdFromToken(token);
        return problemSetService.readProblemSet(githubId);
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
        String githubId = getGithubIdFromToken(token);
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
        String githubId = getGithubIdFromToken(token);
        if (!problemSetService.deleteProblemSet(problemSetId, githubId))
            throw new RuntimeException("알수없는 이유로 삭제에 실패했습니다. 다시 시도해주세요");
        return ResponseEntity.ok("삭제되었습니다");
    }

    private String getGithubIdFromToken(String token) {
        // accessToken 얻기
        if (token == null || token.isBlank() || !token.startsWith("Bearer "))
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        String accessToken = token.replace("Bearer ", "").trim();

        // 토큰 디코딩 및 깃헙 아이디 추출
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);
        return tokenInfo.getGithubId();
    }
}
