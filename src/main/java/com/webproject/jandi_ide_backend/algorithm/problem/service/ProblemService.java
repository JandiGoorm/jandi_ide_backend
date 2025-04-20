package com.webproject.jandi_ide_backend.algorithm.problem.service;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemRequestDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.repository.ProblemRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
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
