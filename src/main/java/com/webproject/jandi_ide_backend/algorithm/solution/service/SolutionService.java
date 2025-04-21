package com.webproject.jandi_ide_backend.algorithm.solution.service;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.algorithm.solution.repository.SolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SolutionService {
    
    private final SolutionRepository solutionRepository;
    
    public SolutionService(SolutionRepository solutionRepository) {
        this.solutionRepository = solutionRepository;
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
} 