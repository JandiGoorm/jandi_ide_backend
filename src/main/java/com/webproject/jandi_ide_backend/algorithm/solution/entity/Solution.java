package com.webproject.jandi_ide_backend.algorithm.solution.entity;

import com.webproject.jandi_ide_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 사용자가 제출한 풀이 엔티티
@Entity
@Table(name = "solutions")
@Getter
@Setter
@NoArgsConstructor
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제출한 사용자와의 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 문제 ID 저장
    @Column(name = "problem_id", nullable = false)
    private Integer problemId;

    // 사용자가 작성한 코드 (풀이 내용)
    @Column(name = "code", columnDefinition = "TEXT", nullable = false)
    private String code;

    // 사용자가 선택한 프로그래밍 언어
    @Column(name = "language", nullable = false)
    private String language;

    // 풀이에 소요된 시간 (분 단위)
    @Column(name = "solving_time")
    private Integer solvingTime;

    // 정답 여부
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    // 추가 정보 (JSON 형식으로 저장 가능)
    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    // 제출 상태 (제출됨, 평가 중, 평가 완료 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SolutionStatus status;

    // 메모리 사용량 (KB)
    @Column(name = "memory_usage")
    private Integer memoryUsage;

    // 실행 시간 (ms)
    @Column(name = "execution_time")
    private Integer executionTime;

    // 설명 필드
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 엔티티 생성 시 자동으로 현재 시각이 기록 (수정 불가능)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 업데이트 시 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 솔루션 상태를 나타내는 열거형
    public enum SolutionStatus {
        SUBMITTED,        // 제출됨
        EVALUATING,       // 평가 중
        CORRECT,          // 정답
        WRONG_ANSWER,     // 오답
        RUNTIME_ERROR,    // 런타임 에러
        COMPILATION_ERROR,// 컴파일 에러
        TIMEOUT,          // 시간 초과
        MEMORY_LIMIT      // 메모리 초과
    }
}
