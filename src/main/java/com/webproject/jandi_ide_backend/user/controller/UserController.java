package com.webproject.jandi_ide_backend.user.controller;


import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.user.dto.AuthResponseDTO;
import com.webproject.jandi_ide_backend.user.dto.UserResponseDTO;
import com.webproject.jandi_ide_backend.user.dto.AuthRequestDTO;
import com.webproject.jandi_ide_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


/**
 * 사용자 관련 API를 처리하는 컨트롤러.
 * 사용자 로그인, 정보 조회 기능을 제공합니다.
 */
@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "깃허브 로그인", description = "GitHub OAuth code 를 이용하여 로그인 후 access token 을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        String code = request.getCode();
        // 1) code가 없으면 에러
        if (code == null || code.isBlank()) {
            throw new CustomException(CustomErrorCodes.INVALID_GITHUB_CODE);
        }

        // 2) 깃헙 OAuth 로그인 처리 (깃헙에 토큰 요청 -> accessToken 발급 ->  DB 저장/조회)
        AuthResponseDTO loginResp;
        try {
            loginResp = userService.login(request);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.GITHUB_LOGIN_FAILED);
        }

        // 3) 최종적으로 토큰 반환
        return ResponseEntity.ok(loginResp);
    }

    @GetMapping("/me")
    @Operation(summary = "사용자 정보 조회", description = "사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
    })
    public ResponseEntity<?> getMe(
            @Parameter(
                name = "Authorization",
                description = "액세스 토큰을 입력해주세요",
                required = true,
                example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token) {
        // 1) 토큰이 없으면 에러
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_GITHUB_TOKEN);
        }

        // 2) 사용자 정보 조회
        UserResponseDTO userProfile;
        String accessToken = token.replace("Bearer ", "").trim();
        userProfile = userService.getMe(accessToken);

        // 3) 사용자 정보 반환
        return ResponseEntity.ok(userProfile);
    }
}
