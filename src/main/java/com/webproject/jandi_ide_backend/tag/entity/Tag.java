package com.webproject.jandi_ide_backend.tag.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 태그(Tag)를 표현하는 엔티티 클래스
 * 각 태그의 이름은 중복 없이 유니크하게 관리
 */
@Entity
@Table(name = "Tag", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class Tag {

    // 고유 식별자 (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 태그의 이름 (반드시 입력되어야 하며, 중복될 수 없음)
    @Column(nullable = false)
    private String name;

    // 엔티티 생성 시 자동으로 현재 시각이 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 수정 시 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}