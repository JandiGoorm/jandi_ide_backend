package com.webproject.jandi_ide_backend.company.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 회사 정보를 표현하는 엔티티 클래스
 * 회사 이름은 유니크 제약 조건으로 중복 저장이 불가능하도록 설정
 */
@Entity
@Table(name = "Company", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class Company {

    // 고유 식별자로 AUTO_INCREMENT를 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 회사 이름 (반드시 입력되어야 하며, 중복될 수 없음)
    @Column(nullable = false)
    private String name;

    // 회사의 상세 설명을 저장하는 컬럼 (TEXT 타입으로 긴 내용을 저장 가능)
    @Column(columnDefinition = "TEXT")
    private String description;

    // 엔티티 생성 시 자동으로 현재 시각이 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 수정 시 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}