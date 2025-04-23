package com.webproject.jandi_ide_backend.user.controller;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.dto.ReqTechStackDTO;
import com.webproject.jandi_ide_backend.tech.dto.RespTechStackDTO;
import com.webproject.jandi_ide_backend.user.service.UserTechStackService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tech-stack/favorite")
public class FavoriteTechStackController {
    private final UserTechStackService userTechStackService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    public List<RespTechStackDTO> getFavoriteTechStack(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token
    ) {
        String githubId = getGithubIdFromToken(token);
        return userTechStackService.readFavoriteTechStack(githubId);
    }

    @PutMapping("")
    public List<RespTechStackDTO> putFavoriteTechStack(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestBody ReqTechStackDTO reqDTO
    ) {
        // 새 언어 리스트가 없다면 에러 반환
        if (reqDTO == null || reqDTO.getTechStackNameList() == null || reqDTO.getTechStackNameList().isEmpty())
            throw new RuntimeException("선택된 언어가 없습니다");

        String githubId = getGithubIdFromToken(token);
        return userTechStackService.putFavoriteTechStack(githubId, reqDTO.getTechStackNameList());
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
