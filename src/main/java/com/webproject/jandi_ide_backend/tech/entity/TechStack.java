package com.webproject.jandi_ide_backend.tech.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 기술 스택(또는 프로그래밍 언어)을 표현하는 엔티티 클래스
 * 각 기술 스택의 이름은 중복 없이 유니크하게 관리
 */
@Entity
@Table(name = "TechStack", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class TechStack {

    // 고유 식별자로 AUTO_INCREMENT를 사용하여 값이 자동 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 기술 스택의 이름을 저장
    // 반드시 입력되어야 하며, 중복된 값은 허용되지 않음
    @Column(nullable = false)
    private String name;

    // 엔티티가 생성될 때 현재 시각이 자동으로 저장
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티가 업데이트될 때마다 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
