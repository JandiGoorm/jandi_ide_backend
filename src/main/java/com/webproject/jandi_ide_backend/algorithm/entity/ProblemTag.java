package com.webproject.jandi_ide_backend.algorithm.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.webproject.jandi_ide_backend.tag.entity.Tag;

/**
 * 문제(Problem)와 태그(Tag) 간의 연결(조인) 관계를 표현하는 엔티티
 */
@Entity
@Table(name = "problem_tags")
@Getter
@Setter
@NoArgsConstructor
public class ProblemTag {

    // 고유 식별자로, AUTO_INCREMENT를 통해 값이 자동 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 연결된 문제 엔티티(Problem)를 참조
    // Lazy Fetching을 사용하여 실제 필요한 경우에만 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problemId", nullable = false)
    private Problem problem;

    // 연결된 태그 엔티티(Tag)를 참조
    // Lazy Fetching을 사용하여 실제 사용할 때만 데이터를 로드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagId", nullable = false)
    private Tag tag;

    // 엔티티 생성 시 자동으로 현재 시간으로 설정되며, 이후 수정할 수 없음
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 업데이트 시 자동으로 현재 시간으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
