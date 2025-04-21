package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.company.entity.Company;
import lombok.Data;

import java.util.List;

@Data
public class PostRespProblemSetDTO {
    private final Long id;
    private final Boolean isCompanyProb;
    private final List<Integer> problemIds;
    private final Integer minutes;
    private final String title;
    private final String company;

    public PostRespProblemSetDTO(ProblemSet problemSet) {
        this.id = problemSet.getId();
        this.isCompanyProb = problemSet.getIsPrevious();
        this.problemIds = problemSet.getProblems();
        this.minutes = problemSet.getSolvingTimeInMinutes();
        this.title = problemSet.getTitle();
        this.company = (problemSet.getCompany() == null) ?
                "" : problemSet.getCompany().getCompanyName();
    }

    public static PostRespProblemSetDTO fromEntity(ProblemSet problemSet) {
        return new PostRespProblemSetDTO(problemSet);
    }
}
