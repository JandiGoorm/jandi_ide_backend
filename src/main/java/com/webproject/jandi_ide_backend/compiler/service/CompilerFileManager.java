package com.webproject.jandi_ide_backend.compiler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 컴파일러 파일 관리 클래스
 * 
 * 코드 처리를 위한 파일 및 디렉토리 생성, 관리, 삭제를 담당하는 유틸리티 클래스
 */
@Slf4j
@Component
public class CompilerFileManager {
    
    private static final String BASE_DIR = "compiler_workspace";
    
    /**
     * 사용자 ID와 문제 ID를 기반으로 작업 디렉토리 경로를 생성하고 디렉토리를 만듭니다.
     * 
     * @param userId 사용자 ID
     * @param problemId 문제 ID
     * @return 생성된 작업 디렉토리 경로
     * @throws IOException 디렉토리 생성 중 오류 발생 시
     */
    public Path createWorkingDir(Long userId, Long problemId) throws IOException {
        // 기본 디렉토리 구조: compiler_workspace/user_{userId}/problem_{problemId}/
        String userDir = "user_" + userId;
        String problemDir = "problem_" + problemId;
        Path workingDir = Paths.get(BASE_DIR, userDir, problemDir);
        
        // 디렉토리 생성
        Files.createDirectories(workingDir);
        log.debug("Created working directory: {}", workingDir);
        
        return workingDir;
    }
    
    /**
     * 작업 디렉토리에 Java 코드 파일을 생성합니다.
     * 
     * @param workingDir 작업 디렉토리 경로
     * @param code 저장할 Java 코드
     * @return 생성된 파일 객체
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    public File createJavaFile(Path workingDir, String code) throws IOException {
        File javaFile = workingDir.resolve("Main.java").toFile();
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }
        log.debug("Created Java file: {}", javaFile.getAbsolutePath());
        return javaFile;
    }
    
    /**
     * 작업 디렉토리에 Python 코드 파일을 생성합니다.
     * 
     * @param workingDir 작업 디렉토리 경로
     * @param code 저장할 Python 코드
     * @return 생성된 파일 객체
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    public File createPythonFile(Path workingDir, String code) throws IOException {
        File pythonFile = workingDir.resolve("Main.py").toFile();
        try (FileWriter writer = new FileWriter(pythonFile)) {
            writer.write(code);
        }
        log.debug("Created Python file: {}", pythonFile.getAbsolutePath());
        return pythonFile;
    }
    
    /**
     * 작업 디렉토리에 C++ 코드 파일을 생성합니다.
     * 
     * @param workingDir 작업 디렉토리 경로
     * @param code 저장할 C++ 코드
     * @return 생성된 파일 객체
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    public File createCppFile(Path workingDir, String code) throws IOException {
        File cppFile = workingDir.resolve("Main.cpp").toFile();
        try (FileWriter writer = new FileWriter(cppFile)) {
            writer.write(code);
        }
        log.debug("Created C++ file: {}", cppFile.getAbsolutePath());
        return cppFile;
    }
    
    /**
     * 작업 디렉토리와 그 내용을 재귀적으로 삭제합니다.
     * 
     * @param dir 삭제할 디렉토리
     * @return 삭제 성공 여부
     */
    public boolean cleanupDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return true;
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanupDirectory(file);
                } else {
                    if (!file.delete()) {
                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
                    } else {
                        log.debug("Deleted file: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        
        boolean deleted = dir.delete();
        if (deleted) {
            log.debug("Deleted directory: {}", dir.getAbsolutePath());
        } else {
            log.warn("Failed to delete directory: {}", dir.getAbsolutePath());
        }
        return deleted;
    }
    
    /**
     * 지정된 경로의 파일을 삭제합니다.
     * 
     * @param file 삭제할 파일
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.debug("Deleted file: {}", file.getAbsolutePath());
            } else {
                log.warn("Failed to delete file: {}", file.getAbsolutePath());
            }
            return deleted;
        }
        return true;
    }
    
    /**
     * 작업 디렉토리에 대한 전체 경로를 반환합니다.
     * 
     * @param workingDir 작업 디렉토리 상대 경로
     * @return 절대 경로
     */
    public String getAbsolutePath(Path workingDir) {
        return workingDir.toAbsolutePath().toString();
    }
    
    /**
     * 기본 컴파일러 작업 디렉토리를 생성합니다.
     * 애플리케이션 시작 시 호출됩니다.
     */
    public void initCompilerWorkspace() {
        try {
            Path baseDir = Paths.get(BASE_DIR);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
                log.info("Created compiler workspace directory: {}", baseDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create compiler workspace directory", e);
        }
    }
} 