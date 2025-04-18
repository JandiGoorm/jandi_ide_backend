package com.webproject.jandi_ide_backend.company.service;

import com.webproject.jandi_ide_backend.company.dto.CompanyCreateRequestDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyResponseDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyUpdateRequestDTO;
import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * 전체 기업 목록 조회
     * @return List<CompanyResponseDTO>
     */
    public List<CompanyResponseDTO> findAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 기업 추가
     * @param requestDTO CompanyCreateRequestDTO
     * @return CompanyResponseDTO
     */
    public CompanyResponseDTO postCompany(CompanyCreateRequestDTO requestDTO){
        Company company = new Company();
        company.setName(requestDTO.getName());
        company.setDescription(requestDTO.getDescription());

        try{
            companyRepository.save(company);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToResponseDTO(company);
    }

    /**
     * 기업 정보 수정
     * @param requestDTO CompanyUpdateRequestDTO
     * @param id Integer
     * @return CompanyResponseDTO
     */
    public CompanyResponseDTO updateCompany(CompanyUpdateRequestDTO requestDTO , Integer id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.COMPANY_NOT_FOUND));

        company.setName(requestDTO.getName());
        company.setDescription(requestDTO.getDescription());

        try{
            companyRepository.save(company);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToResponseDTO(company);
    }

    /**
     * 기업 삭제
     * @param id Integer
     */
    public void deleteCompany(Integer id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.COMPANY_NOT_FOUND));

        try{
            companyRepository.delete(company);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }
    }

    private CompanyResponseDTO convertToResponseDTO(Company company) {
        CompanyResponseDTO responseDTO = new CompanyResponseDTO();
        responseDTO.setId(company.getId());
        responseDTO.setName(company.getName());
        responseDTO.setDescription(company.getDescription());
        responseDTO.setCreatedAt(company.getCreatedAt());
        responseDTO.setUpdatedAt(company.getUpdatedAt());
        return responseDTO;
    }
}
