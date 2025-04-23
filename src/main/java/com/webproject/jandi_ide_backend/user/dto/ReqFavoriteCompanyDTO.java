package com.webproject.jandi_ide_backend.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 선호 기업 추가, 수정 요청에 사용하는 DTO */
@Data
@NoArgsConstructor(force = true)
public class ReqFavoriteCompanyDTO {
    List<String> companyNameList;
}
