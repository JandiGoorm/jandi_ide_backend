package com.webproject.jandi_ide_backend.jobPosting.service;

import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.jobPosting.dto.PostingRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.PostingResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.entity.JobPosting;
import com.webproject.jandi_ide_backend.jobPosting.repository.JobPostingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobPostingService {
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingScheduleService jobPostingScheduleService;

    public JobPostingService(CompanyRepository companyRepository, JobPostingRepository jobPostingRepository, JobPostingScheduleService jobPostingScheduleService) {
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.jobPostingScheduleService = jobPostingScheduleService;
    }

    /**
     * 특정 기업의 채용 공고 추가
     * @param requestDTO :PostingCreateRequestDTO
     * @param id :Integer 기업의 id
     * @return PostingResponseDTO
     */
    public PostingResponseDTO postJobPosting(PostingRequestDTO requestDTO, Integer id) {
        Company company = companyRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.COMPANY_NOT_FOUND));
        JobPosting jobPosting = new JobPosting();
        jobPosting.setTitle(requestDTO.getTitle());
        jobPosting.setDescription(requestDTO.getDescription());
        jobPosting.setCompany(company);

        try{
            jobPostingRepository.save(jobPosting);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToPostingResponseDTO(jobPosting);
    }

    /**
     * 채용 공고 수정
     * @param requestDTO :PostingRequestDTO
     * @param id :Integer 채용 공고의 id
     * @return PostingResponseDTO
     */
    public PostingResponseDTO updateJobPosting(PostingRequestDTO requestDTO, Integer id) {
        JobPosting jobPosting = jobPostingRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.JOBPOSTING_NOT_FOUND));
        jobPosting.setTitle(requestDTO.getTitle());
        jobPosting.setDescription(requestDTO.getDescription());

        try{
            jobPostingRepository.save(jobPosting);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToPostingResponseDTO(jobPosting);
    }

    /**
     * 채용 공고 삭제
     * @param id :Integer 채용 공고의 id
     */
    public void deleteJobPosting(Integer id) {
        JobPosting jobPosting = jobPostingRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.JOBPOSTING_NOT_FOUND));

        try{
            jobPostingRepository.delete(jobPosting);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }
    }


    private PostingResponseDTO convertToPostingResponseDTO(JobPosting jobposting) {
        PostingResponseDTO postingResponseDTO = new PostingResponseDTO();
        postingResponseDTO.setTitle(jobposting.getTitle());
        postingResponseDTO.setDescription(jobposting.getDescription());
        postingResponseDTO.setId(jobposting.getId());
        postingResponseDTO.setCreatedAt(jobposting.getCreatedAt());
        postingResponseDTO.setUpdatedAt(jobposting.getUpdatedAt());

        List<ScheduleResponseDTO> scheduleDTOs = jobposting.getSchedules().stream()
                .map(jobPostingScheduleService::convertToScheduleResponseDTO)
                .toList();

        postingResponseDTO.setSchedules(scheduleDTOs);


        return postingResponseDTO;
    }
}
