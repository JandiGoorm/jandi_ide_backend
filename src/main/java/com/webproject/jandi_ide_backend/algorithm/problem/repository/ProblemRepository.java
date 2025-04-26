package com.webproject.jandi_ide_backend.algorithm.problem.repository;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Optional<Problem> findById(Integer id);

    List<Problem> findByLevel(Integer level);
    
    // 난이도 오름차순 정렬
    Page<Problem> findAllByOrderByLevelAsc(Pageable pageable);
    
    // 난이도 내림차순 정렬
    Page<Problem> findAllByOrderByLevelDesc(Pageable pageable);
}
