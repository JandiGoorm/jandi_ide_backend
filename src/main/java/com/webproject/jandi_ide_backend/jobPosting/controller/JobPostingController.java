package com.webproject.jandi_ide_backend.jobPosting.controller;

import com.webproject.jandi_ide_backend.jobPosting.dto.PostingRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.PostingResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.service.JobPostingScheduleService;
import com.webproject.jandi_ide_backend.jobPosting.service.JobPostingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="JobPosting", description = "채용 공고 관련 API")
@RestController
@RequestMapping("/api/job-postings")
public class JobPostingController {
    private final JobPostingService jobPostingService;
    private final JobPostingScheduleService jobPostingScheduleService;

    public JobPostingController(JobPostingService jobPostingService, JobPostingScheduleService jobPostingScheduleService) {
        this.jobPostingService = jobPostingService;
        this.jobPostingScheduleService = jobPostingScheduleService;
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "채용 공고 수정 (STAFF 이상)",description="기업의 채용 공고를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = PostingResponseDTO.class))
            ),
    })
    public ResponseEntity<PostingResponseDTO>updateJobPosting(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @RequestBody PostingRequestDTO postingRequestDTO,
            @PathVariable Integer id
    ){
        PostingResponseDTO postingResponseDTO = jobPostingService.updateJobPosting(postingRequestDTO,id);
        return ResponseEntity.ok(postingResponseDTO);
    }
    
    @DeleteMapping({"/{id}"})
    @Operation(summary = "채용 공고 삭제 (STAFF 이상)",description="기업의 채용 공고를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "채용 공고 삭제 성공")
                    )
            )
    })
    public ResponseEntity<String>deleteJobPosting(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @PathVariable int id
    ){
        jobPostingService.deleteJobPosting(id);
        
        return ResponseEntity.ok("채용 공고 삭제 성공");
    }

    @PostMapping("/{id}/schedule")
    @Operation(summary = "공고의 일정 추가 (STAFF 이상)",description="해당 공고의 일정을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDTO.class))
            ),
    })
    public ResponseEntity<ScheduleResponseDTO>postSchedule(
            @Parameter(
                    name = "Authorization",
                    description = "액세스 토큰을 입력해주세요",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String token,
            @RequestBody ScheduleRequestDTO requestDTO,
            @PathVariable Integer id
    ){
        ScheduleResponseDTO scheduleResponseDTO = jobPostingScheduleService.postSchedule(requestDTO,id);
        return ResponseEntity.ok(scheduleResponseDTO);
    }
}
