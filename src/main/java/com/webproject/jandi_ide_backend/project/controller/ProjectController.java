package com.webproject.jandi_ide_backend.project.controller;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.project.dto.BlobResponseDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectCreateRequestDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectResponseDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectUpdateRequestDTO;
import com.webproject.jandi_ide_backend.project.entity.Project;
import com.webproject.jandi_ide_backend.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "대표 프로젝트 추가", description = "자신의 대표 프로젝트를 추가합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
    })
    public ResponseEntity<ProjectResponseDTO> postProject(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestBody ProjectCreateRequestDTO requestDTO
    ){
        ProjectResponseDTO projectResponseDTO = projectService.postProject(token,requestDTO);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 프로젝트 조회", description = "특정 프로젝트를 조회합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Object.class))
            ),
    })
    public ResponseEntity<?> getProject(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id
    ){
        Object projectResponseDTO = projectService.getProject(token,id);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "대표 프로젝트 수정", description = "자신의 대표 프로젝트를 수정합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
    })
    public ResponseEntity<ProjectResponseDTO> putProject(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestBody ProjectUpdateRequestDTO requestDTO
    ){
        ProjectResponseDTO projectResponseDTO = projectService.updateProject(token,id, requestDTO);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "대표 프로젝트 삭제", description = "자신의 대표 프로젝트를 삭제합니다.",security = { @SecurityRequirement(name = "Authorization") })
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
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id
    ){
        projectService.deleteProject(token, id);
        return ResponseEntity.ok("프로젝트 삭제 성공");
    }

    @GetMapping("/{id}/blob")
    @Operation(summary = "특정 프로젝트의 blob 조회", description = "특정 프로젝트의 blob을 조회합니다.",security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BlobResponseDTO.class))
            ),
    })
    public ResponseEntity<BlobResponseDTO> getProjectBlob(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestParam(value = "sha") String sha
    ){
        BlobResponseDTO projectResponseDTO = projectService.getProjectBlob(token,id, sha);
        return ResponseEntity.ok(projectResponseDTO);
    }
}
