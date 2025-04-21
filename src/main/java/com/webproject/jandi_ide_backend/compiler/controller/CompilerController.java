package com.webproject.jandi_ide_backend.compiler.controller;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.service.CompilerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    private final CompilerService compilerService;
    
    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }
    
    /**
     * 코드를 제출받아 컴파일 및 실행 후 결과를 반환합니다.
     * @param submissionDto 제출된 코드 정보
     * @return 저장된 Solution 엔티티
     */
    @PostMapping("/submit")
    public ResponseEntity<Solution> submitCode(@RequestBody CodeSubmissionDto submissionDto) {
        Solution solution = compilerService.submitCode(submissionDto);
        return ResponseEntity.ok(solution);
    }
} 