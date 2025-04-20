package com.webproject.jandi_ide_backend.jobPosting.controller;

import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.service.JobPostingScheduleService;
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

@Tag(name="JobPostingSchedule", description = "공고의 일정 관련 API")
@RestController
@RequestMapping("/api/schedules")
public class JobPostingScheduleController {
    private final JobPostingScheduleService jobPostingScheduleService;

    public JobPostingScheduleController(JobPostingScheduleService jobPostingScheduleService) {
        this.jobPostingScheduleService = jobPostingScheduleService;
    }

    @PutMapping("/{id}")
    @Operation(summary = "일정 수정 (STAFF 이상)",description="일정을 수정합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDTO.class))
            ),
    })
    public ResponseEntity<ScheduleResponseDTO> updateJobPostingSchedule(
            @RequestBody ScheduleRequestDTO requestDTO,
            @PathVariable Integer id
    ){
        ScheduleResponseDTO scheduleResponseDTO = jobPostingScheduleService.updateSchedule(requestDTO,id);
        return ResponseEntity.ok(scheduleResponseDTO);
    }

    @DeleteMapping({"/{id}"})
    @Operation(summary = "일정 삭제 (STAFF 이상)",description="일정을 삭제합니다.", security = { @SecurityRequirement(name = "Authorization") })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "일정 삭제 성공")
                    )
            )
    })
    public ResponseEntity<String>deleteSchedule(@PathVariable int id) {
        jobPostingScheduleService.deleteSchedule(id);

        return ResponseEntity.ok("일정삭제 성공");
    }
}
