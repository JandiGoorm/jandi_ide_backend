package com.webproject.jandi_ide_backend.user.dto;

import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.user.entity.UserFavoriteCompany;
import lombok.AllArgsConstructor;
import lombok.Data;

/** 선호 기업 응답에 사용하는 DTO */
@Data
public class RespFavoriteCompanyDTO {
    private final Integer id;
    private final String companyName;

    public RespFavoriteCompanyDTO(Company company) {
        this.id = company.getId();
        this.companyName = company.getCompanyName();
    }
}
