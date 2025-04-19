package com.webproject.jandi_ide_backend.company.dto;

import com.webproject.jandi_ide_backend.jobPosting.entity.JobPosting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyDetailResponseDTO extends CompanyResponseDTO{

    @Schema (description = "기업의 채용 공고")
    private List<JobPosting> jobPostings = new ArrayList<>();
}
