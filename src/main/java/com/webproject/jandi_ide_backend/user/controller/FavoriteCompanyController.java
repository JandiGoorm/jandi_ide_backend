package com.webproject.jandi_ide_backend.user.controller;

import com.webproject.jandi_ide_backend.user.dto.ReqFavoriteCompanyDTO;
import com.webproject.jandi_ide_backend.user.dto.RespFavoriteCompanyDTO;
import com.webproject.jandi_ide_backend.user.service.FavoriteCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies/favorite")
@Tag(name = "즐겨찾는 기업 API", description = "사용자의 관심 기업 관리를 위한 API")
public class FavoriteCompanyController {
    private final FavoriteCompanyService favoriteCompanyService;

    // 기업 조회
    @GetMapping("")
    @Operation(
        summary = "즐겨찾는 기업 목록 조회",
        description = "사용자가 즐겨찾는 기업 목록을 조회합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    public List<RespFavoriteCompanyDTO> getFavoriteCompany(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token
    ) {
        return favoriteCompanyService.readFavoriteCompanyByToken(token);
    }

    // 기업 리스트 추가
    @PostMapping("")
    @Operation(
        summary = "즐겨찾는 기업 목록 설정",
        description = "사용자의 즐겨찾는 기업 목록을 새로 설정합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    public List<RespFavoriteCompanyDTO> postFavoriteCompany(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "설정할 기업 이름 목록", required = true) @RequestBody ReqFavoriteCompanyDTO reqDTO
    ) {
        return favoriteCompanyService.postFavoriteCompanyByToken(token, reqDTO.getCompanyNameList());
    }

    // 단일 기업 추가
    @PutMapping("/{companyId}")
    @Operation(
        summary = "즐겨찾는 기업 추가",
        description = "특정 기업을 사용자의 즐겨찾는 기업 목록에 추가합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 기업 추가됨"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 또는 이미 추가된 기업")
    })
    public ResponseEntity<?> putFavoriteCompany (
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "추가할 기업 ID", required = true) @PathVariable Integer companyId
    ){
        boolean isSucceed = favoriteCompanyService.putFavoriteCompanyByToken(token, companyId);
        if (!isSucceed)
            throw new RuntimeException("알 수 없는 이유로 실패했습니다. 다시 시도해 주세요.");
        return ResponseEntity.ok().body("추가되었습니다");
    }

    // 단일 기업 삭제
    @DeleteMapping("/{companyId}")
    @Operation(
        summary = "즐겨찾는 기업 삭제",
        description = "특정 기업을 사용자의 즐겨찾는 기업 목록에서 삭제합니다.",
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 기업 삭제됨"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 또는 존재하지 않는 기업")
    })
    public ResponseEntity<?> deleteFavoriteCompany (
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "삭제할 기업 ID", required = true) @PathVariable Integer companyId
    ){
        boolean isSucceed = favoriteCompanyService.deleteFavoriteCompanyByToken(token, companyId);
        if (!isSucceed)
            throw new RuntimeException("알 수 없는 이유로 실패했습니다. 다시 시도해 주세요.");
        return ResponseEntity.ok().body("삭제되었습니다");
    }
}
