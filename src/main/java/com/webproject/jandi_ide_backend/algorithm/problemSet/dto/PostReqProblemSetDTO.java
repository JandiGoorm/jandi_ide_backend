package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(force = true)
public class PostReqProblemSetDTO {
    //필수
    @NonNull
    private final Boolean isCompanyProb;
    @NonNull
    private final Integer minutes;
    @NonNull
    private final String title;

    //선택적
    private List<Integer> problemIds; //커스텀일 때만 필수
    private String companyName; //기업문제일 때만 필수
}
