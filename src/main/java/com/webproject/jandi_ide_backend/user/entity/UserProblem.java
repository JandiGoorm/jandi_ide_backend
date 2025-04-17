package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 이 엔티티는 사용자가 해결한 문제의 기록을 저장
 * 각 기록은 특정 유저와 특정 문제 사이의 관계를 나타내며,
 * 문제 해결 상태(예: "solved")와 함께 생성/수정 시각이 자동으로 기록
 */
@Entity
@Table(name = "UserProblem")
@Getter
@Setter
@NoArgsConstructor
public class UserProblem {

    // 고유 식별자: AUTO_INCREMENT로 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 문제를 풀었던 유저를 참조
    // 실제로 데이터가 필요한 시점에만 로딩하도록 Lazy 로딩을 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 해결한 문제를 참조
    // Lazy 로딩을 사용하여 실제 필요할 때만 관련 데이터를 불러옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problemId", nullable = false)
    private Problem problem;

    // 문제 해결 상태를 저장 (예: "solved")
    // 상태값은 반드시 입력되어야 함
    @Column(nullable = false)
    private String status;

    // 이 레코드가 생성될 때 자동으로 현재 시각이 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이 레코드가 수정될 때 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
