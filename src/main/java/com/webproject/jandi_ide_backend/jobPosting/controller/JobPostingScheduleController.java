package com.webproject.jandi_ide_backend.jobPosting.controller;

import com.webproject.jandi_ide_backend.jobPosting.dto.PostingResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.service.JobPostingScheduleService;
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

@Tag(name="JobPostingSchedule", description = "공고의 일정 관련 API")
@RestController
@RequestMapping("/api/schedules")
public class JobPostingScheduleController {
    private final JobPostingScheduleService jobPostingScheduleService;

    public JobPostingScheduleController(JobPostingScheduleService jobPostingScheduleService) {
        this.jobPostingScheduleService = jobPostingScheduleService;
    }

    @GetMapping
    @Operation(
            summary = "월별 채용 일정 조회",
            description = "지정된 년도와 월에 해당하는 모든 채용 일정을 조회합니다.",
            security = { @SecurityRequirement(name = "Authorization") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PostingResponseDTO.class))
                    )
            ),
    })
    public ResponseEntity<List<PostingResponseDTO>> getMonthlySchedules(
            @Parameter(description = "조회할 년도", example = "2025", required = true) @RequestParam Integer year,
            @Parameter(description = "조회할 월(1-12)", example = "4", required = true) @RequestParam Integer month,
            @Parameter(hidden = true) @RequestParam(value = "Authorization", required = false) String token
    ) {
        List<PostingResponseDTO> schedules = jobPostingScheduleService.getSchedulesByYearAndMonth(year, month);
        return ResponseEntity.ok(schedules);
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
