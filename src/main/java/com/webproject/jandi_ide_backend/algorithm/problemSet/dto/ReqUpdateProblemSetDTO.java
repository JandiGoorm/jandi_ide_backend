package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor(force = true)
public class ReqUpdateProblemSetDTO {
    @NonNull
    private final String title;
}
