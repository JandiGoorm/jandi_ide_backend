package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemDetailResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespDetailProblemSet {
    private Long id;
    private String title;
    private Boolean isPrevious;
    private Integer solvingTimeInMinutes;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProblemDetailResponseDTO> problems;
}
