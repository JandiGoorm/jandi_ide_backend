package com.webproject.jandi_ide_backend.company.controller;

import com.webproject.jandi_ide_backend.company.dto.CompanyCreateRequestDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyResponseDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyUpdateRequestDTO;
import com.webproject.jandi_ide_backend.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Company", description = "기업 관련 API")
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    @Operation(summary = "기업 목록 조회", description = "모든 기업의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CompanyResponseDTO.class))
                    )
            )
    })
    public ResponseEntity<List<CompanyResponseDTO>> findAllCompanies(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token
    ) {
        List<CompanyResponseDTO> companies = companyService.findAllCompanies();
        return ResponseEntity.ok(companies);
    }

    @PostMapping
    @Operation(summary = "기업 추가 (STAFF 이상)", description = "기업을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = CompanyResponseDTO.class))
            ),
    })
    public ResponseEntity<CompanyResponseDTO> postCompany(
       @Parameter(
               name = "Authorization",
               description = "액세스 토큰을 입력해주세요",
               required = true,
               example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
       )
       @RequestHeader("Authorization") String token,
       @RequestBody CompanyCreateRequestDTO companyCreateRequestDTO
    ){
        return ResponseEntity.ok(companyService.postCompany(companyCreateRequestDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "기업 수정 (STAFF 이상)", description = "기업 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = CompanyResponseDTO.class))
            ),
    })
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @Parameter(description = "기업 ID", example = "1") @PathVariable Integer id,
            @RequestBody CompanyUpdateRequestDTO companyUpdateRequestDTO
    ){
        return ResponseEntity.ok(companyService.updateCompany(companyUpdateRequestDTO, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "기업 삭제 (STAFF 이상)", description = "기업을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
    })
    public ResponseEntity<String> deleteCompany(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @Parameter(description = "기업 ID", example = "1") @PathVariable Integer id
    ){
        companyService.deleteCompany(id);
        return ResponseEntity.ok("기업 삭제 성공");
    }
}
