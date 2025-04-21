package com.webproject.jandi_ide_backend.algorithm.solution.repository;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {
    // 추가적인 쿼리 메서드는 필요에 따라 구현할 수 있습니다.
} 