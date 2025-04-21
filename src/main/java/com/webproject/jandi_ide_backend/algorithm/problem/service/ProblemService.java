package com.webproject.jandi_ide_backend.algorithm.problem.service;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemDetailResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.repository.ProblemRepository;
import com.webproject.jandi_ide_backend.algorithm.testCase.dto.TestCaseResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.algorithm.testCase.repository.TestCaseRepository;
import com.webproject.jandi_ide_backend.algorithm.testCase.service.TestCaseService;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseService testCaseService;

    public ProblemService(ProblemRepository problemRepository, TestCaseRepository testCaseRepository, TestCaseService testCaseService) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.testCaseService = testCaseService;
    }

    public List<ProblemResponseDTO> getProblems() {
        return problemRepository.findAll().stream()
                .map(this::convertToProblemResponseDTO)
                .toList();
    }

    public ProblemResponseDTO postProblem(ProblemRequestDTO problemRequestDTO) {
        Problem problem = new Problem();
        problem.setDescription(problemRequestDTO.getDescription());
        problem.setLevel(problemRequestDTO.getLevel());
        problem.getTags().addAll(problemRequestDTO.getTags());
        problem.setMemory(problemRequestDTO.getMemory());
        problem.setTimeLimit(problemRequestDTO.getTimeLimit());

        try{
            problemRepository.save(problem);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToProblemResponseDTO(problem);
    }

    public ProblemResponseDTO updateProblem(ProblemRequestDTO problemRequestDTO,Integer id) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.PROBLEM_NOT_FOUND));

        problem.setDescription(problemRequestDTO.getDescription());
        problem.setLevel(problemRequestDTO.getLevel());
        problem.setMemory(problemRequestDTO.getMemory());
        problem.setTimeLimit(problemRequestDTO.getTimeLimit());

        problem.getTags().clear();
        problem.getTags().addAll(problemRequestDTO.getTags());

        try{
            problemRepository.save(problem);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        return convertToProblemResponseDTO(problem);
    }

    public ProblemDetailResponseDTO getProblemDetail(Integer id){
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.PROBLEM_NOT_FOUND));
        List<TestCase> testCases = testCaseRepository.findByProblemId(id);
        List<TestCaseResponseDTO> testCaseDTOs = testCases.stream()
                .map(testCaseService::convertToDTO)
                .collect(Collectors.toList());

        ProblemDetailResponseDTO detailDTO = new ProblemDetailResponseDTO();
        detailDTO.setId(problem.getId());
        detailDTO.setDescription(problem.getDescription());
        detailDTO.setLevel(problem.getLevel());
        detailDTO.setMemory(problem.getMemory());
        detailDTO.setTimeLimit(problem.getTimeLimit());
        detailDTO.setTestCases(testCaseDTOs);
        detailDTO.setTags(problem.getTags());
        detailDTO.setCreatedAt(problem.getCreatedAt());
        detailDTO.setUpdatedAt(problem.getUpdatedAt());

        return detailDTO;
    }

    public void deleteProblem(Integer id){
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.PROBLEM_NOT_FOUND));

        try{
            problemRepository.delete(problem);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }
    }

    private ProblemResponseDTO convertToProblemResponseDTO(Problem problem) {
        ProblemResponseDTO problemResponseDTO = new ProblemResponseDTO();
        problemResponseDTO.setId(problem.getId());
        problemResponseDTO.setDescription(problem.getDescription());
        problemResponseDTO.setLevel(problem.getLevel());
        problemResponseDTO.setMemory(problem.getMemory());
        problemResponseDTO.setTimeLimit(problem.getTimeLimit());
        problemResponseDTO.setTags(problem.getTags());
        problemResponseDTO.setCreatedAt(problem.getCreatedAt());
        problemResponseDTO.setUpdatedAt(problem.getUpdatedAt());

        return problemResponseDTO;
    }
}
