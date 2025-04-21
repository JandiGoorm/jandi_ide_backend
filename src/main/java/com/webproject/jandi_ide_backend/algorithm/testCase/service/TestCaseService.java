package com.webproject.jandi_ide_backend.algorithm.testCase.service;

import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.algorithm.testCase.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseService {
    
    private final TestCaseRepository testCaseRepository;
    
    /**
     * 주어진 문제 ID에 해당하는 모든 테스트 케이스를 조회합니다.
     * @param problemId 조회할 문제의 ID
     * @return 테스트 케이스 목록
     */
    public List<TestCase> getTestCasesByProblemId(Integer problemId) {
        return testCaseRepository.findByProblemId(problemId);
    }
} 