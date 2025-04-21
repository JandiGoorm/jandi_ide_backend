package com.webproject.jandi_ide_backend.algorithm.testCase.repository;

import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
    
    /**
     * 문제 ID에 해당하는 모든 테스트 케이스를 조회합니다.
     */
    @Query("SELECT tc FROM TestCase tc WHERE tc.problem.id = :problemId")
    List<TestCase> findByProblemId(@Param("problemId") Integer problemId);
} 