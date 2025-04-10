package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.webproject.jandi_ide_backend.algorithm.entity.Problem;

/**
 * 문제집(Basket)과 문제(Problem) 간의 다대다 관계를 관리하는 조인 테이블 엔티티
 * 각 인스턴스는 하나의 문제집과 하나의 문제 사이의 연결(관계)을 나타냄
 */
@Entity
@Table(name = "BasketProblem")
@Getter
@Setter
@NoArgsConstructor
public class BasketProblem {

    // 고유 식별자 (AUTO_INCREMENT 사용)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 연결된 문제집(Basket) 엔티티 참조
    // Lazy 로딩을 사용하여 실제 필요한 시점에만 문제집 정보를 불러옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basketId", nullable = false)
    private Basket basket;

    // 연결된 문제(Problem) 엔티티 참조
    // Lazy 로딩을 사용하여 실제 필요한 시점에만 문제 정보를 불러옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problemId", nullable = false)
    private Problem problem;

    // 엔티티 생성 시 현재 시각을 자동으로 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 업데이트 시 현재 시각으로 자동 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
