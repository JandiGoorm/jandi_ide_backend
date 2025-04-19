package com.webproject.jandi_ide_backend.jobPosting.repository;

import com.webproject.jandi_ide_backend.jobPosting.entity.JobPostingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPostingScheduleRepository extends JpaRepository<JobPostingSchedule, Long> {
    Optional<JobPostingSchedule> findById(Integer id);
}
