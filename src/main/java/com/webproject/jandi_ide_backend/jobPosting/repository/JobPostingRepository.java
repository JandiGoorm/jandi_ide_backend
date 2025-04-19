package com.webproject.jandi_ide_backend.jobPosting.repository;

import com.webproject.jandi_ide_backend.jobPosting.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    Optional<JobPosting> findById(Integer id);
}
