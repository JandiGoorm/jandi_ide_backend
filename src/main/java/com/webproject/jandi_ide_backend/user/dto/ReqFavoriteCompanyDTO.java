package com.webproject.jandi_ide_backend.user.dto;

import lombok.Data;

import java.util.List;

/** 선호 기업 추가, 수정 요청에 사용하는 DTO */
@Data
public class ReqFavoriteCompanyDTO {
    private final List<String> companyNameList;

    ReqFavoriteCompanyDTO(List<String> companyNameList) {
        this.companyNameList = companyNameList;
    }
}
