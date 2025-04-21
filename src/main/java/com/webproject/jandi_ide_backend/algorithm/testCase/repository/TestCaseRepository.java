package com.webproject.jandi_ide_backend.algorithm.testCase.repository;

import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    Optional<TestCase> findById(Integer id);

    List<TestCase> findByProblemId(Integer problemId);
}
