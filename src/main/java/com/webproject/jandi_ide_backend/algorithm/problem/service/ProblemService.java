package com.webproject.jandi_ide_backend.algorithm.problem.service;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemDetailResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemPageResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public ProblemPageResponseDTO getProblems(Integer page, Integer size, String sort, String direction) {
        long totalItems = problemRepository.count();
        int totalPages = (int)Math.ceil((double)totalItems/size);

        if(page < 0 || page >= totalPages){
            throw new CustomException(CustomErrorCodes.INVALID_PAGE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Problem> problemPage;
        
        // 정렬 옵션 처리
        if (sort != null && sort.equalsIgnoreCase("level")) {
            if (direction != null && direction.equalsIgnoreCase("desc")) {
                problemPage = problemRepository.findAllByOrderByLevelDesc(pageable);
            } else {
                problemPage = problemRepository.findAllByOrderByLevelAsc(pageable);
            }
        } else {
            problemPage = problemRepository.findAll(pageable);
        }

        List<ProblemResponseDTO> problemDTOs = problemPage.getContent().stream()
                .map(this::convertToProblemResponseDTO)
                .toList();

        ProblemPageResponseDTO responseDTO = new ProblemPageResponseDTO();
        responseDTO.setData(problemDTOs);
        responseDTO.setCurrentPage(problemPage.getNumber());
        responseDTO.setSize(problemPage.getSize());
        responseDTO.setTotalItems(problemPage.getTotalElements());
        responseDTO.setTotalPages(problemPage.getTotalPages());

        return responseDTO;
    }

    public ProblemResponseDTO postProblem(ProblemRequestDTO problemRequestDTO) {
        Problem problem = new Problem();
        problem.setTitle(problemRequestDTO.getTitle());
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

        problem.setTitle(problemRequestDTO.getTitle());
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
        detailDTO.setTitle(problem.getTitle());
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

    /**
     * ID로 문제를 조회합니다.
     * @param id 조회할 문제의 ID
     * @return 문제 객체
     * @throws RuntimeException 문제를 찾을 수 없는 경우
     */
    public Problem getProblemById(Integer id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.PROBLEM_NOT_FOUND));
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
        problemResponseDTO.setTitle(problem.getTitle());

        return problemResponseDTO;
    }
}
