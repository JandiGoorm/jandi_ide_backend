package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.webproject.jandi_ide_backend.tech.entity.TechStack;

// 유저와 선호 기술 스택(TechStack) 간의 다대다 관계를 관리하는 조인 테이블 엔티티
@Entity
@Table(name = "UserTechStack")
@Getter
@Setter
@NoArgsConstructor
public class UserTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 선호하는 기술 스택을 보유한 유저를 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 선호하는 기술 스택(TechStack)을 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "techStackId", nullable = false)
    private TechStack techStack;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
