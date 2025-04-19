package com.webproject.jandi_ide_backend.algorithm.problemSet.entity;

import com.webproject.jandi_ide_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 문제집 엔티티
@Entity
@Table(name = "problem_sets")
@Getter
@Setter
@NoArgsConstructor
public class ProblemSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기출 문제집 여부 (true: 기출, false: 사용자 생성)
    @Column(name = "is_previous", nullable = false)
    private Boolean isPrevious;

    // 문제집에 포함된 문제 ID 목록
    @ElementCollection
    @CollectionTable(name = "problem_set_problems", joinColumns = @JoinColumn(name = "problem_set_id"))
    @Column(name = "problem_id")
    private List<Integer> problems = new ArrayList<>();

    // 문제집 풀이 제한 시간 (분 단위)
    @Column(name = "solving_time_in_minutes")
    private Integer solvingTimeInMinutes;

    // 문제집 설명 정보
    @Column(name = "description")
    private String description;

    // 문제집 생성 사용자와의 관계 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 문제집 생성 시간
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 문제집 최종 수정 시간
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 엔티티 생성 시 자동으로 호출되어 생성 시간과 수정 시간을 현재 시간으로 설정
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티 수정 시 자동으로 호출되어 수정 시간을 현재 시간으로 업데이트
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
