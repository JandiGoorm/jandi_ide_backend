package com.webproject.jandi_ide_backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 선호 기업 추가, 수정 요청에 사용하는 DTO */
@Getter
@NoArgsConstructor(force = true)
public class ReqFavoriteCompanyDTO {
    private final List<String> companyNameList;
}
