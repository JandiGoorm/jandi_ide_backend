package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(force = true)
public class PostReqProblemSetDTO {
    private String companyName;
    @NonNull
    private final Boolean isCompanyProb;
    @NonNull
    private final List<Integer> problemIds;
    @NonNull
    private final Integer minutes;
    @NonNull
    private final String title;
}
