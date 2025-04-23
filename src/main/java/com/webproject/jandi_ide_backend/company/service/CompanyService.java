package com.webproject.jandi_ide_backend.company.service;

import com.webproject.jandi_ide_backend.company.dto.CompanyDetailResponseDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyRequestDTO;
import com.webproject.jandi_ide_backend.company.dto.CompanyResponseDTO;
import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.jobPosting.dto.PostingResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.entity.JobPosting;
import com.webproject.jandi_ide_backend.jobPosting.repository.JobPostingScheduleRepository;
import com.webproject.jandi_ide_backend.jobPosting.service.JobPostingScheduleService;
import com.webproject.jandi_ide_backend.jobPosting.service.JobPostingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final JobPostingScheduleService jobPostingScheduleService;

    public CompanyService(CompanyRepository companyRepository, JobPostingScheduleService jobPostingScheduleService, JobPostingScheduleRepository jobPostingScheduleRepository, JobPostingService jobPostingService) {
        this.companyRepository = companyRepository;
        this.jobPostingScheduleService = jobPostingScheduleService;
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
     * 특정 기업 조회
     * @return CompanyDetailResponse
     */
    public CompanyDetailResponseDTO findCompanyById(Integer id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.COMPANY_NOT_FOUND));

        return convertToDetailResponseDTO(company);
    }

    /**
     * 기업 추가
     * @param requestDTO CompanyRequestDTO
     * @return CompanyResponseDTO
     */
    public CompanyResponseDTO postCompany(CompanyRequestDTO requestDTO){
        Company company = new Company();
        company.setCompanyName(requestDTO.getCompanyName());
        company.setDescription(requestDTO.getDescription());
        company.setTimeInMinutes(requestDTO.getTimeInMinutes());

        company.getProgrammingLanguages().addAll(requestDTO.getProgrammingLanguages());
        company.getLevels().addAll(requestDTO.getLevels());

        try{
            companyRepository.save(company);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToResponseDTO(company);
    }

    /**
     * 기업 정보 수정
     * @param requestDTO CompanyRequestDTO
     * @param id Integer
     * @return CompanyResponseDTO
     */
    public CompanyResponseDTO updateCompany(CompanyRequestDTO requestDTO , Integer id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.COMPANY_NOT_FOUND));

        company.setCompanyName(requestDTO.getCompanyName());
        company.setDescription(requestDTO.getDescription());
        company.setTimeInMinutes(requestDTO.getTimeInMinutes());

        company.getLevels().clear();
        company.getLevels().addAll(requestDTO.getLevels());
        company.getProgrammingLanguages().clear();
        company.getProgrammingLanguages().addAll(requestDTO.getProgrammingLanguages());

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

    private CompanyDetailResponseDTO convertToDetailResponseDTO(Company company){
        CompanyDetailResponseDTO responseDTO = new CompanyDetailResponseDTO();
        responseDTO.setId(company.getId());
        responseDTO.setCompanyName(company.getCompanyName());
        responseDTO.setDescription(company.getDescription());
        responseDTO.setTimeInMinutes(company.getTimeInMinutes());
        responseDTO.setLevels(company.getLevels());
        responseDTO.setProgrammingLanguages(company.getProgrammingLanguages());
        responseDTO.setCreatedAt(company.getCreatedAt());
        responseDTO.setUpdatedAt(company.getUpdatedAt());

        List<PostingResponseDTO> jobPostingDTOs = company.getJobPostings().stream()
                .map(this::convertToPostingResponseDTO)
                .toList();

        responseDTO.setJobPostings(jobPostingDTOs);

        return responseDTO;
    }

    private PostingResponseDTO convertToPostingResponseDTO(JobPosting posting) {
        PostingResponseDTO dto = new PostingResponseDTO();
        dto.setId(posting.getId());
        dto.setTitle(posting.getTitle());
        dto.setDescription(posting.getDescription());
        dto.setCreatedAt(posting.getCreatedAt());
        dto.setUpdatedAt(posting.getUpdatedAt());

        List<ScheduleResponseDTO> scheduleDTOs = posting.getSchedules().stream()
                .map(jobPostingScheduleService::convertToScheduleResponseDTO)
                .toList();

        dto.setSchedules(scheduleDTOs);

        return dto;
    }

    private CompanyResponseDTO convertToResponseDTO(Company company) {
        CompanyResponseDTO responseDTO = new CompanyResponseDTO();
        responseDTO.setId(company.getId());
        responseDTO.setCompanyName(company.getCompanyName());
        responseDTO.setDescription(company.getDescription());
        responseDTO.setLevels(company.getLevels());
        responseDTO.setTimeInMinutes(company.getTimeInMinutes());
        responseDTO.setProgrammingLanguages(company.getProgrammingLanguages());
        responseDTO.setCreatedAt(company.getCreatedAt());
        responseDTO.setUpdatedAt(company.getUpdatedAt());
        responseDTO.setProfileUrl(company.getProfileUrl());
        return responseDTO;
    }

}
