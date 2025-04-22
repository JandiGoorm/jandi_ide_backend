package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import lombok.Getter;

import java.util.List;

@Getter
public class RespSpecProblemSetDTO extends RespProblemSetDTO {
    private final List<Problem> ProblemList;

    public RespSpecProblemSetDTO(ProblemSet problemSet, List<Problem> problemList) {
        super(problemSet);
        this.ProblemList = problemList;
    }
}
