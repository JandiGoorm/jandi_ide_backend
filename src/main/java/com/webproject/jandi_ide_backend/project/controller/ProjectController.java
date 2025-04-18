package com.webproject.jandi_ide_backend.project.controller;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.project.dto.BlobResponseDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectCreateRequestDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectResponseDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectUpdateRequestDTO;
import com.webproject.jandi_ide_backend.project.entity.Project;
import com.webproject.jandi_ide_backend.project.repository.ProjectRepository;
import com.webproject.jandi_ide_backend.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



/**
 * 대표 프로젝트 관련 API를 처리하는 컨트롤러.
 */
@Tag(name="Project", description = "대표 프로젝트 관련 API")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectService projectService, ProjectRepository projectRepository) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
    }

    @PostMapping
    @Operation(summary = "대표 프로젝트 추가", description = "자신의 대표 프로젝트를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
    })
    public ResponseEntity<ProjectResponseDTO> postProject(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @RequestBody ProjectCreateRequestDTO requestDTO
    ){
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }


        String accessToken = token.replace("Bearer ", "").trim();
        ProjectResponseDTO projectResponseDTO = projectService.postProject(accessToken, requestDTO);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 프로젝트 조회", description = "특정 프로젝트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Object.class))
            ),
    })
    public ResponseEntity<?> getProject(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @PathVariable int id
    ){
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "").trim();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.PROJECT_NOT_FOUND));
        Object projectResponseDTO = projectService.getProject(accessToken,project.getOwner().getGithubUsername(),project.getGithubName());
        return ResponseEntity.ok(projectResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "대표 프로젝트 수정", description = "자신의 대표 프로젝트를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
    })
    public ResponseEntity<ProjectResponseDTO> putProject(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @PathVariable int id,
            @RequestBody ProjectUpdateRequestDTO requestDTO
    ){
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "").trim();
        ProjectResponseDTO projectResponseDTO = projectService.updateProject(accessToken,id, requestDTO);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "대표 프로젝트 삭제", description = "자신의 대표 프로젝트를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "프로젝트 삭제 성공")
                    )
            )
    })
    public ResponseEntity<String> deleteProject(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @PathVariable int id
    ){
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "").trim();
        projectService.deleteProject(accessToken, id);

        return ResponseEntity.ok("프로젝트 삭제 성공");
    }

    @GetMapping("/{id}/blob")
    @Operation(summary = "특정 프로젝트의 blob 조회", description = "특정 프로젝트의 blob을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BlobResponseDTO.class))
            ),
    })
    public ResponseEntity<BlobResponseDTO> getProjectBlob(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @PathVariable int id,
            @RequestParam(value = "sha", required = true) String sha
    ){
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        }

        String accessToken = token.replace("Bearer ", "").trim();
        projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.PROJECT_NOT_FOUND));

        BlobResponseDTO projectResponseDTO = projectService.getProjectBlob(accessToken,id, sha);
        return ResponseEntity.ok(projectResponseDTO);
    }
}
