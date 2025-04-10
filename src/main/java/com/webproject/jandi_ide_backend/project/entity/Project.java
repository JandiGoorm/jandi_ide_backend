package com.webproject.jandi_ide_backend.project.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.webproject.jandi_ide_backend.user.entity.User;

/**
 * 프로젝트 정보를 표현하는 엔티티 클래스
 * 프로젝트는 사용자의 GitHub 레포지토리와 연동
 */
@Entity
@Table(name = "Project")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    // 고유 식별자 (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 프로젝트 이름 (반드시 입력되어야 함)
    @Column(nullable = false)
    private String name;

    // 프로젝트에 대한 상세 설명을 저장 (긴 텍스트를 저장할 수 있도록 TEXT 타입 사용)
    @Column(columnDefinition = "TEXT")
    private String description;

    // 프로젝트 관련 URL (GitHub 링크)
    private String url;

    // 프로젝트 소유자(User)와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User owner;

    // 엔티티 생성 시 자동으로 현재 시각이 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 수정 시 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
