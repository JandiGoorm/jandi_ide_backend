package com.webproject.jandi_ide_backend.algorithm.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 알고리즘 문제를 표현하는 엔티티 클래스
 */
@Entity
@Table(name = "Problem")
@Getter
@Setter
@NoArgsConstructor
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 문제의 제목 또는 이름을 저장
    @Column(nullable = false)
    private String name;

    // 문제의 난이도를 저장
    @Column(nullable = false)
    private String difficulty;

    // 문제의 상세 설명 및 내용을 저장 (긴 텍스트를 저장할 수 있도록 TEXT 타입 사용)
    @Column(columnDefinition = "TEXT")
    private String content;

    // 엔티티 생성 시 자동으로 현재 시간이 입력 (수정 불가)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 업데이트 시 자동으로 현재 시간으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
