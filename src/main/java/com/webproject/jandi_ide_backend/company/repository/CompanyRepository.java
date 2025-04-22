package com.webproject.jandi_ide_backend.company.repository;

import com.webproject.jandi_ide_backend.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository  extends JpaRepository<Company, Long> {
    Optional<Company> findById(Integer id);

    Optional<Company> findByCompanyName(String companyName);
}
