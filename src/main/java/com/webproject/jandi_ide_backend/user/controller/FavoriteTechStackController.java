package com.webproject.jandi_ide_backend.user.controller;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.dto.ReqTechStackDTO;
import com.webproject.jandi_ide_backend.tech.dto.RespTechStackDTO;
import com.webproject.jandi_ide_backend.user.service.UserTechStackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tech-stack/favorite")
@Tag(name = "즐겨찾는 기술 스택 API", description = "사용자의 관심 기술 스택 관리를 위한 API")
public class FavoriteTechStackController {
    private final UserTechStackService userTechStackService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    @Operation(
        summary = "즐겨찾는 기술 스택 조회",
        description = "사용자가 즐겨찾는 기술 스택 목록을 조회합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    public List<RespTechStackDTO> getFavoriteTechStack(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token
    ) {
        String githubId = getGithubIdFromToken(token);
        return userTechStackService.readFavoriteTechStack(githubId);
    }

    @PutMapping("")
    @Operation(
        summary = "즐겨찾는 기술 스택 업데이트",
        description = "사용자의 즐겨찾는 기술 스택 목록을 갱신합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    public List<RespTechStackDTO> putFavoriteTechStack(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "설정할 기술 스택 이름 목록", required = true) @RequestBody ReqTechStackDTO reqDTO
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
