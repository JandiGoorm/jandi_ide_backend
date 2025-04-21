package com.webproject.jandi_ide_backend.algorithm.problem.service;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.repository.ProblemRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;

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

    /**
     * 모든 문제를 조회합니다.
     * @return 문제 목록
     */
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }
    
    /**
     * ID로 문제를 조회합니다.
     * @param id 조회할 문제의 ID
     * @return 문제 객체
     * @throws RuntimeException 문제를 찾을 수 없는 경우
     */
    public Problem getProblemById(Integer id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
    }
}
