package com.webproject.jandi_ide_backend.jobPosting.service;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.jobPosting.dto.PostingResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleRequestDTO;
import com.webproject.jandi_ide_backend.jobPosting.dto.ScheduleResponseDTO;
import com.webproject.jandi_ide_backend.jobPosting.entity.JobPosting;
import com.webproject.jandi_ide_backend.jobPosting.entity.JobPostingSchedule;
import com.webproject.jandi_ide_backend.jobPosting.repository.JobPostingRepository;
import com.webproject.jandi_ide_backend.jobPosting.repository.JobPostingScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        return convertToScheduleResponseDTO(jobPostingSchedule);
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

        return convertToScheduleResponseDTO(jobPostingSchedule);
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

    public List<PostingResponseDTO> getSchedulesByYearAndMonth(Integer year, Integer month){
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 해당 년도와 월에 속하는 모든 스케줄 조회
        List<JobPostingSchedule> schedules = jobPostingScheduleRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);

        // 채용공고 ID 별로 그룹화
        Map<JobPosting, List<JobPostingSchedule>> schedulesByPosting = schedules.stream()
                .collect(Collectors.groupingBy(JobPostingSchedule::getJobPosting));

        List<PostingResponseDTO> result = new ArrayList<>();

        schedulesByPosting.forEach((jobPosting, postingSchedules) -> {
            PostingResponseDTO postingDTO = new PostingResponseDTO();
            postingDTO.setId(jobPosting.getId());
            postingDTO.setTitle(jobPosting.getTitle());
            postingDTO.setDescription(jobPosting.getDescription());
            postingDTO.setCreatedAt(jobPosting.getCreatedAt());
            postingDTO.setUpdatedAt(jobPosting.getUpdatedAt());

            // 스케줄 DTO 변환
            List<ScheduleResponseDTO> scheduleResponseDTOs = postingSchedules.stream()
                    .map(this::convertToScheduleResponseDTO)
                    .collect(Collectors.toList());

            postingDTO.setSchedules(scheduleResponseDTOs);
            result.add(postingDTO);
        });

        return result;
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
