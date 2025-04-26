package com.webproject.jandi_ide_backend.user.controller;

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

    @GetMapping("")
    @Operation(
        summary = "즐겨찾는 기술 스택 조회",
        description = "사용자가 즐겨찾는 기술 스택 목록을 조회합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    public List<RespTechStackDTO> getFavoriteTechStack(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token
    ) {
        return userTechStackService.readFavoriteTechStackByToken(token);
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
        return userTechStackService.putFavoriteTechStackByToken(token, reqDTO.getTechStackNameList());
    }
}
