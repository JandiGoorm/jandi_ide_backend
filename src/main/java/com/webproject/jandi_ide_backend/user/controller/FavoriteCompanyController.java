package com.webproject.jandi_ide_backend.user.controller;

import com.webproject.jandi_ide_backend.user.dto.ReqFavoriteCompanyDTO;
import com.webproject.jandi_ide_backend.user.dto.RespFavoriteCompanyDTO;
import com.webproject.jandi_ide_backend.user.service.FavoriteCompanyService;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies/favorite")
public class FavoriteCompanyController {
    private final FavoriteCompanyService favoriteCompanyService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    public List<RespFavoriteCompanyDTO> getFavoriteCompany(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token
    ) {
        String githubId = getGithubIdFromToken(token);
        return favoriteCompanyService.readFavoriteCompany(githubId);
    }

    @PostMapping("")
    public List<RespFavoriteCompanyDTO> PostFavoriteCompany(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestBody ReqFavoriteCompanyDTO reqDTO
    ) {
        // 새 기업 리스트가 없다면 에러 반환
        if (reqDTO == null || reqDTO.getCompanyNameList() == null || reqDTO.getCompanyNameList().isEmpty())
            throw new RuntimeException("선택된 기업이 없습니다");

        String githubId = getGithubIdFromToken(token);
        return favoriteCompanyService.postFavoriteCompany(githubId, reqDTO.getCompanyNameList());
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
