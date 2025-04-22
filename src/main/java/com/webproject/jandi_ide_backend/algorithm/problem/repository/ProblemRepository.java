package com.webproject.jandi_ide_backend.algorithm.problem.repository;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Optional<Problem> findById(Integer id);

    List<Problem> findByLevel(Integer level);
}
