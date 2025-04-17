package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;

import com.webproject.jandi_ide_backend.company.entity.Company;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

// 유저와 관심 기업 간의 관계를 관리하는 조인 테이블 엔티티
@Entity
@Table(name = "UserFavoriteCompany")
@Getter
@Setter
@NoArgsConstructor
public class UserFavoriteCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 관심 기업을 등록한 유저를 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 관심 기업(Company)을 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyId", nullable = false)
    private Company company;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
