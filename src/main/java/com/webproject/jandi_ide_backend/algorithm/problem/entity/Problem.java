package com.webproject.jandi_ide_backend.algorithm.problem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "problems")
@Getter
@Setter
@NoArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "memory", nullable = false)
    private Integer memory;  // 메모리 제한 (MB)

    @Column(name = "time_limit", nullable = false)
    private Integer timeLimit;  // 시간 제한 (seconds)

    @ElementCollection
    @CollectionTable(
            name = "problem_tags",
            joinColumns = @JoinColumn(name = "problem_id")
    )
    @Column(name = "tag_name")
    private Set<String> tags = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
