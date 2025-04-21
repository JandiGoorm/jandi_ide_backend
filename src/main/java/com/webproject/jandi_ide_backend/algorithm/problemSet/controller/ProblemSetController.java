package com.webproject.jandi_ide_backend.algorithm.problemSet.controller;

import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.ReqPostProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.ReqUpdateProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.service.ProblemSetService;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("")
    public Object createProblemSet(
            @RequestHeader("Authorization") String token,
            @RequestBody ReqPostProblemSetDTO probSetDTO
    ) {
        String githubId = getGithubIdFromToken(token);
        return problemSetService.createProblemSet(probSetDTO, githubId);
    }

    /// read
    @GetMapping("")
    public Object readProblemSet(
            @RequestHeader("Authorization") String token
    ) {
        String githubId = getGithubIdFromToken(token);
        return problemSetService.readProblemSet(githubId);
    }

    /// update
    @PutMapping("{problemSetId}")
    public Object updateProblemSet(
            @RequestHeader("Authorization") String token,
            @PathVariable Long problemSetId,
            @RequestBody ReqUpdateProblemSetDTO probSetDTO
            ) {
        String githubId = getGithubIdFromToken(token);
        return problemSetService.updateProblemSet(problemSetId, probSetDTO, githubId);
    }

    /// delete
    @DeleteMapping("{problemSetId}")
    public ResponseEntity<?> deleteProblemSet(
            @RequestHeader("Authorization") String token,
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
