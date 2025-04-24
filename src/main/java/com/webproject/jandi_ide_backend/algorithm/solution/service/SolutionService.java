package com.webproject.jandi_ide_backend.algorithm.solution.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.service.ProblemService;
import com.webproject.jandi_ide_backend.algorithm.problemSet.Repository.ProblemSetRepository;
import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.algorithm.problemSet.service.ProblemSetService;
import com.webproject.jandi_ide_backend.algorithm.solution.dto.ProblemSetSolutionsDto;
import com.webproject.jandi_ide_backend.algorithm.solution.dto.SolutionResponseDto;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.algorithm.solution.repository.SolutionRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SolutionService {
    
    private final SolutionRepository solutionRepository;
    private final ProblemService problemService;
    private final ProblemSetService problemSetService;
    private final ProblemSetRepository problemSetRepository;
    
    public SolutionService(SolutionRepository solutionRepository, 
                         ProblemService problemService,
                         ProblemSetService problemSetService,
                         ProblemSetRepository problemSetRepository) {
        this.solutionRepository = solutionRepository;
        this.problemService = problemService;
        this.problemSetService = problemSetService;
        this.problemSetRepository = problemSetRepository;
    }
    
    /**
     * 제출된 솔루션을 저장합니다.
     * @param solution 저장할 솔루션 객체
     * @return 저장된 솔루션 객체
     */
    @Transactional
    public Solution saveSolution(Solution solution) {
        return solutionRepository.save(solution);
    }
    
    /**
     * ID로 솔루션을 조회합니다.
     * @param id 조회할 솔루션의 ID
     * @return 솔루션 객체 (Optional)
     */
    public Optional<Solution> findById(Long id) {
        return solutionRepository.findById(id);
    }
    
    /**
     * 모든 솔루션을 조회합니다.
     * @return 솔루션 리스트
     */
    public List<Solution> findAll() {
        return solutionRepository.findAll();
    }
    
    /**
     * 특정 문제에 대한 성공한 솔루션들을 조회합니다.
     * @param problemId 문제 ID
     * @return 성공한 솔루션 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<SolutionResponseDto> findSuccessfulSolutions(Integer problemId) {
        List<Solution> solutions = solutionRepository.findByProblemIdAndIsCorrectTrue(problemId);
        return solutions.stream()
                .map(SolutionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 문제집에 대한 사용자의 솔루션을 조회합니다.
     * @param userId 사용자 ID
     * @param problemSetId 문제집 ID
     * @return 문제집의 문제와 사용자의 솔루션 정보 DTO
     */
    @Transactional(readOnly = true)
    public ProblemSetSolutionsDto findUserProblemSetSolutions(Long userId, Long problemSetId) {
        // 문제집 조회
        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.PROBLEMSET_NOT_FOUND));
        
        // 문제집에 포함된 문제 ID 리스트 조회
        List<Integer> problemIds = problemSet.getProblems();
        
        // 문제별 솔루션 리스트 생성
        List<ProblemSetSolutionsDto.ProblemSolutionDto> problemSolutions = new ArrayList<>();
        
        for (Integer problemId : problemIds) {
            // 문제 정보 조회
            Problem problem = problemService.getProblemById(problemId);
            
            // 사용자의 해당 문제에 대한 솔루션 조회 (가장 최근 제출)
            Optional<Solution> latestSolution = solutionRepository
                    .findTopByUserIdAndProblemIdAndProblemSetIdOrderByCreatedAtDesc(
                            userId, problemId, problemSetId);
            
            // DTO 생성
            ProblemSetSolutionsDto.ProblemSolutionDto problemSolutionDto = 
                    ProblemSetSolutionsDto.ProblemSolutionDto.builder()
                            .problemId(problem.getId())
                            .problemTitle(problem.getTitle())
                            .level(problem.getLevel())
                            .problemDescription(problem.getDescription())
                            .solution(latestSolution.map(SolutionResponseDto::fromEntity).orElse(null))
                            .build();
            
            problemSolutions.add(problemSolutionDto);
        }
        
        // 최종 응답 DTO 생성
        return ProblemSetSolutionsDto.builder()
                .problemSetId(problemSetId)
                .problemSetName(problemSet.getTitle())
                .userId(userId)
                .problems(problemSolutions)
                .build();
    }
    
    /**
     * 특정 사용자의 특정 문제에 대한 솔루션을 조회합니다.
     * @param userId 사용자 ID
     * @param problemId 문제 ID
     * @return 솔루션 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<SolutionResponseDto> findUserSolutionsForProblem(Long userId, Integer problemId) {
        List<Solution> solutions = solutionRepository.findByUserIdAndProblemId(userId, problemId);
        return solutions.stream()
                .map(SolutionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
} 