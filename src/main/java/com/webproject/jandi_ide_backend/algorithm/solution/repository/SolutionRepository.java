package com.webproject.jandi_ide_backend.algorithm.solution.repository;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {
    
    /**
     * 특정 문제ID에 해당하는 성공한 솔루션 리스트를 조회합니다.
     */
    List<Solution> findByProblemIdAndIsCorrectTrue(Integer problemId);
    
    /**
     * 특정 사용자ID와 문제ID에 해당하는 솔루션 리스트를 조회합니다.
     */
    List<Solution> findByUserIdAndProblemId(Long userId, Integer problemId);
    
    /**
     * 특정 사용자ID, 문제ID, 문제집ID에 해당하는 솔루션 중 가장 최근에 제출된 솔루션을 조회합니다.
     */
    Optional<Solution> findTopByUserIdAndProblemIdAndProblemSetIdOrderByCreatedAtDesc(
            Long userId, Integer problemId, Long problemSetId);
    
    /**
     * 특정 사용자ID와 문제집ID에 해당하는 솔루션 리스트를 조회합니다.
     */
    List<Solution> findByUserIdAndProblemSetId(Long userId, Long problemSetId);
} 