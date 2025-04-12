package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.webproject.jandi_ide_backend.tech.entity.TechStack;

/**
 * 유저와 선호 기술 스택(TechStack) 간의 다대다 관계를 관리하는 조인 테이블 엔티티
 * 각 인스턴스는 특정 유저가 특정 기술 스택을 선호한다는 관계를 나타냄
 */
@Entity
@Table(name = "UserTechStack")
@Getter
@Setter
@NoArgsConstructor
public class UserTechStack {

    // 고유 식별자: AUTO_INCREMENT를 사용하여 값이 자동 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 선호하는 기술 스택을 보유한 유저를 참조
    // Lazy 로딩을 사용하여 실제 데이터가 필요한 시점에만 불러옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 선호하는 기술 스택(TechStack)을 참조
    // Lazy 로딩으로 실제 객체가 필요한 경우에만 로드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "techStackId", nullable = false)
    private TechStack techStack;

    // 이 레코드가 생성될 때 자동으로 현재 시각을 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이 레코드가 수정될 때마다 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
