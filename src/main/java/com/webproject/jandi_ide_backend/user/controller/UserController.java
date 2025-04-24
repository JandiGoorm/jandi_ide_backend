package com.webproject.jandi_ide_backend.user.controller;


import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.project.dto.ProjectPageResponseDTO;
import com.webproject.jandi_ide_backend.project.service.ProjectService;
import com.webproject.jandi_ide_backend.user.dto.*;
import com.webproject.jandi_ide_backend.user.service.UserService;
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


/**
 * 사용자 관련 API를 처리하는 컨트롤러.
 * 사용자 로그인, 정보 조회 기능을 제공합니다.
 */
@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final ProjectService projectService;

    public UserController(UserService userService, ProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @Operation(summary = "깃허브 로그인", description = "GitHub OAuth code 를 이용하여 로그인 후 JWT 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        String code = request.getCode();
        // 1) code가 없으면 에러
        if (code == null || code.isBlank()) {
            throw new CustomException(CustomErrorCodes.INVALID_GITHUB_CODE);
        }

        // 2) 깃헙 OAuth 로그인 처리 (깃헙에 토큰 요청 -> accessToken 발급 ->  DB 저장/조회)
        AuthResponseDTO loginResp;
        loginResp = userService.login(request);

        // 3) 최종적으로 토큰 반환
        return ResponseEntity.ok(loginResp);
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
    })
    public ResponseEntity<UserResponseDTO> getMe(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        UserResponseDTO userProfile = userService.getMe(token);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/refresh")
    @Operation(summary = "리프레시 토큰 갱신", description = "리프레시 토큰을 이용하여 새로운 JWT 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리프레시 토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
    })
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody RefreshDTO request){
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        AuthResponseDTO authRespDTO = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(authRespDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "내 정보 수정", description = "내 정보를 수정합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestBody UserUpdateDTO userUpdateDTO) {
        UserResponseDTO userResponse = userService.updateUser(token, id, userUpdateDTO);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "탈퇴 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id
    ){
        userService.deleteUser(token, id);
        return ResponseEntity.ok("회원 탈퇴 성공");
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
    })
    public ResponseEntity<UserResponseDTO> getUser(
            @PathVariable Integer id) {
        UserResponseDTO userResponse = userService.getUser(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}/repos")
    @Operation(summary = "자신의 깃헙 레포지토리 조회", description = "자신의 레포지토리를 조회합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserRepoDTO.class)))
            ),
    })
    public ResponseEntity<UserRepoDTO[]> getMyRepos(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        UserRepoDTO[] userResponse = userService.getUserRepos(token, id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}/projects")
    @Operation(summary = "특정 유저의 대표 프로젝트 조회", description = "특정 유저의 대표 프로젝트를 조회합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProjectPageResponseDTO.class))
            ),
    })
    public ResponseEntity<ProjectPageResponseDTO> getProjects(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestParam(value="page",defaultValue = "0") Integer page,
            @RequestParam(value="size",defaultValue = "10") Integer size
    ) {
        ProjectPageResponseDTO projectResponseDTOList = projectService.getProjects(token, id, page, size);
        return ResponseEntity.ok(projectResponseDTOList);
    }
}
