package com.webproject.jandi_ide_backend.jobPosting.service;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.entity.JobPosting;
import com.webproject.jandi_ide_backend.jobPosting.entity.JobPostingSchedule;
import com.webproject.jandi_ide_backend.jobPosting.repository.JobPostingRepository;
import com.webproject.jandi_ide_backend.jobPosting.repository.JobPostingScheduleRepository;
import org.springframework.stereotype.Service;

@Service
public class JobPostingScheduleService {
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingScheduleRepository jobPostingScheduleRepository;

    public JobPostingScheduleService(JobPostingRepository jobPostingRepository , JobPostingScheduleRepository jobPostingScheduleRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobPostingScheduleRepository = jobPostingScheduleRepository;
    }

    /**
     * 채용 공고에 일정을 추가합니다.
     * @param requestDTO :ScheduleRequestDTO
     * @param id :Integer 채용 공고 id
     * @return ScheduleResponseDTO
     */
    public ScheduleResponseDTO postSchedule(ScheduleRequestDTO requestDTO,Integer id){
        JobPosting jobPosting = jobPostingRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.JOBPOSTING_NOT_FOUND));

        JobPostingSchedule jobPostingSchedule = new JobPostingSchedule();
        jobPostingSchedule.setJobPosting(jobPosting);
        jobPostingSchedule.setScheduleName(requestDTO.getScheduleName());
        jobPostingSchedule.setDescription(requestDTO.getDescription());
        jobPostingSchedule.setDate(requestDTO.getDate());

        try{
            jobPostingScheduleRepository.save(jobPostingSchedule);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToDTO(jobPostingSchedule);
    }

    /**
     * 채용 공고의 일정을 수정합니다.
     * @param requestDTO :ScheduleRequestDTO
     * @param id :Integer 일정 id
     * @return ScheduleResponseDTO
     */
    public ScheduleResponseDTO updateSchedule(ScheduleRequestDTO requestDTO,Integer id){
        JobPostingSchedule jobPostingSchedule = jobPostingScheduleRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.JOBPOSTING_NOT_FOUND));

        jobPostingSchedule.setScheduleName(requestDTO.getScheduleName());
        jobPostingSchedule.setDescription(requestDTO.getDescription());
        jobPostingSchedule.setDate(requestDTO.getDate());

        try{
            jobPostingScheduleRepository.save(jobPostingSchedule);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToDTO(jobPostingSchedule);
    }

    /**
     * 채용 공고의 일정을 삭제합니다.
     * @param id :Integer 일정 id
     */
    public void deleteSchedule(Integer id) {
        JobPostingSchedule jobPostingSchedule = jobPostingScheduleRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.JOBPOSTING_NOT_FOUND));

        try {
            jobPostingScheduleRepository.delete(jobPostingSchedule);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }
    }

    private ScheduleResponseDTO convertToDTO(JobPostingSchedule jobPostingSchedule){
        ScheduleResponseDTO scheduleResponseDTO = new ScheduleResponseDTO();
        scheduleResponseDTO.setId(jobPostingSchedule.getId());
        scheduleResponseDTO.setScheduleName(jobPostingSchedule.getScheduleName());
        scheduleResponseDTO.setDescription(jobPostingSchedule.getDescription());
        scheduleResponseDTO.setDate(jobPostingSchedule.getDate());
        scheduleResponseDTO.setUpdatedAt(jobPostingSchedule.getUpdatedAt());
        scheduleResponseDTO.setCreatedAt(jobPostingSchedule.getCreatedAt());

        return scheduleResponseDTO;
    }

    public ScheduleResponseDTO convertToScheduleResponseDTO(JobPostingSchedule schedule){
        ScheduleResponseDTO dto = new ScheduleResponseDTO();
        dto.setId(schedule.getId());
        dto.setScheduleName(schedule.getScheduleName());
        dto.setDescription(schedule.getDescription());
        dto.setDate(schedule.getDate());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());

        return dto;
    }
}
