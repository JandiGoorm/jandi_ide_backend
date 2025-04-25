package com.webproject.jandi_ide_backend.project.repository;

import com.webproject.jandi_ide_backend.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository  extends JpaRepository<Project, Long> {
    Optional<Project> findById(Integer id);

    List<Project> findByOwner_Id(Integer userId);

    Page<Project> findByOwner_Id(Integer userId, Pageable pageable);

    long countByOwner_Id(Integer userId);
}
