package com.webproject.jandi_ide_backend.algorithm.testCase.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.repository.ProblemRepository;
import com.webproject.jandi_ide_backend.algorithm.testCase.dto.TestCaseRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.testCase.dto.TestCaseResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.algorithm.testCase.repository.TestCaseRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import org.springframework.stereotype.Service;

@Service
public class TestCaseService {
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public TestCaseService(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    /**
     * 문제에 테스트케이스를 추가합니다.
     * @param requestDTO :TestCaseRequestDTO 요청 body
     * @param id :문제의 id
     * @return TestCaseResponseDTO
     */
    public TestCaseResponseDTO postTestCase(TestCaseRequestDTO requestDTO,Integer id){
        Problem problem = problemRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.PROBLEM_NOT_FOUND));
        TestCase testCase = new TestCase();
        testCase.setInput(requestDTO.getInput());
        testCase.setOutput(requestDTO.getOutput());
        testCase.setProblem(problem);

        try{
            testCaseRepository.save(testCase);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToDTO(testCase);
    }

    /**
     * 테스트케이스를 수정합니다.
     * @param requestDTO :TestCaseRequestDTO 요청 body
     * @param id :테스트케이스의 id
     * @return TestCaseResponseDTO
     */
    public TestCaseResponseDTO updateTestCase(TestCaseRequestDTO requestDTO,Integer id){
        TestCase testCase = testCaseRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.TESTCASE_NOT_FOUND));
        testCase.setInput(requestDTO.getInput());
        testCase.setOutput(requestDTO.getOutput());

        try{
            testCaseRepository.save(testCase);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToDTO(testCase);
    }

    /**
     * 테스트케이스를 삭제합니다.
     * @param id :테스트케이스의 id
     */
    public void deleteTestCase(Integer id){
        TestCase testCase = testCaseRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.TESTCASE_NOT_FOUND));

        try{
            testCaseRepository.delete(testCase);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }
    }

    public TestCaseResponseDTO convertToDTO(TestCase testCase){
        TestCaseResponseDTO dto = new TestCaseResponseDTO();
        dto.setId(testCase.getId());
        dto.setInput(testCase.getInput());
        dto.setOutput(testCase.getOutput());
        dto.setCreatedAt(testCase.getCreatedAt());
        dto.setUpdatedAt(testCase.getUpdatedAt());

        return dto;
    }
}
