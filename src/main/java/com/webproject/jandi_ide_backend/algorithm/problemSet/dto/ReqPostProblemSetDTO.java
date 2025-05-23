package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Getter
@NoArgsConstructor(force = true)
public class ReqPostProblemSetDTO {
    //필수
    @NonNull
    private final Boolean isCompanyProb;
    @NonNull
    private final Integer minutes;
    @NonNull
    private final String title;

    @NonNull
    private final ProblemSet.Language language;

    //선택적
    private List<Integer> problemIds; //커스텀일 때만 필수
    private String companyName; //기업문제일 때만 필수
}
