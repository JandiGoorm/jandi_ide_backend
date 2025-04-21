package com.webproject.jandi_ide_backend.algorithm.problemSet.controller;

import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.PostReqProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.service.ProblemSetService;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
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

    @PostMapping("")
    public Object createProblemSet(
            @RequestHeader("Authorization") String token,
            @RequestBody PostReqProblemSetDTO probSetDTO
    ) {
        // 토큰에서 유저 정보 추출
        if (token == null || token.isBlank() || !token.startsWith("Bearer "))
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        String accessToken = token.replace("Bearer ", "").trim();
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);

        return problemSetService.createProblemSet(probSetDTO, tokenInfo.getGithubId());
    }
}
