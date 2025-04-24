package com.webproject.jandi_ide_backend.company.controller;

import com.webproject.jandi_ide_backend.company.dto.CompanyDetailResponseDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyRequestDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyResponseDTO;
import com.webproject.jandi_ide_backend.company.service.CompanyService;
import com.webproject.jandi_ide_backend.jobPosting.dto.PostingRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.PostingResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.service.JobPostingService;
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

import java.util.List;

@Tag(name="Company", description = "기업 관련 API")
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;
    private final JobPostingService jobPostingService;

    public CompanyController(CompanyService companyService, JobPostingService jobPostingService) {
        this.companyService = companyService;
        this.jobPostingService = jobPostingService;
    }

    @GetMapping
    @Operation(
        summary = "전체 기업 목록 조회", 
        description = "모든 기업의 목록을 조회합니다.", 
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CompanyResponseDTO.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<CompanyResponseDTO>> findAllCompanies() {
        List<CompanyResponseDTO> companies = companyService.findAllCompanies();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "특정 기업 조회", 
        description = "특정 기업의 정보를 조회합니다.", 
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = CompanyDetailResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<CompanyDetailResponseDTO> findCompany(
            @Parameter(description = "기업 ID", required = true, example = "1") 
            @PathVariable Integer id
    ){
        CompanyDetailResponseDTO company = companyService.findCompanyById(id);
        return ResponseEntity.ok(company);
    }

    @PostMapping
    @Operation(
        summary = "기업 추가 (STAFF 이상)", 
        description = "기업을 추가합니다.", 
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "추가 성공",
                content = @Content(schema = @Schema(implementation = CompanyResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (STAFF 이상 필요)")
    })
    public ResponseEntity<CompanyResponseDTO> postCompany(
        @Parameter(
            description = "추가할 기업 정보", 
            required = true, 
            schema = @Schema(implementation = CompanyRequestDTO.class)
        )
        @RequestBody CompanyRequestDTO companyRequestDTO
    ){
        return ResponseEntity.ok(companyService.postCompany(companyRequestDTO));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "기업 수정 (STAFF 이상)", 
        description = "기업 정보를 수정합니다.", 
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "수정 성공",
                content = @Content(schema = @Schema(implementation = CompanyResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (STAFF 이상 필요)"),
            @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
    })
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            @Parameter(description = "기업 ID", required = true, example = "1") 
            @PathVariable Integer id,
            @Parameter(
                description = "수정할 기업 정보", 
                required = true, 
                schema = @Schema(implementation = CompanyRequestDTO.class)
            )
            @RequestBody CompanyRequestDTO companyRequestDTO
    ){
        return ResponseEntity.ok(companyService.updateCompany(companyRequestDTO, id));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "기업 삭제 (STAFF 이상)", 
        description = "기업을 삭제합니다.", 
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "삭제 성공",
                content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (STAFF 이상 필요)"),
            @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
    })
    public ResponseEntity<String> deleteCompany(
            @Parameter(description = "기업 ID", required = true, example = "1") 
            @PathVariable Integer id
    ){
        companyService.deleteCompany(id);
        return ResponseEntity.ok("기업 삭제 성공");
    }

    @PostMapping("/{id}/job-posting")
    @Operation(
        summary = "채용 공고 추가 (STAFF 이상)",
        description = "기업의 채용 공고를 추가합니다.", 
        security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "추가 성공",
                content = @Content(schema = @Schema(implementation = PostingResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (STAFF 이상 필요)"),
            @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
    })
    public ResponseEntity<PostingResponseDTO> postJobPosting(
            @Parameter(
                description = "추가할 채용 공고 정보", 
                required = true, 
                schema = @Schema(implementation = PostingRequestDTO.class)
            )
            @RequestBody PostingRequestDTO postingRequestDTO,
            @Parameter(description = "기업 ID", required = true, example = "1") 
            @PathVariable Integer id
            ) {
        PostingResponseDTO postingResponseDTO = jobPostingService.postJobPosting(postingRequestDTO, id);
        return ResponseEntity.ok(postingResponseDTO);
    }
}
