package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.webproject.jandi_ide_backend.company.entity.Company;

/**
 * 유저와 관심 기업 간의 관계를 관리하는 조인 테이블 엔티티
 * 각 인스턴스는 특정 유저가 특정 회사를 관심 기업으로 등록했음을 나타냄
 */
@Entity
@Table(name = "UserFavoriteCompany")
@Getter
@Setter
@NoArgsConstructor
public class UserFavoriteCompany {

    // 고유 식별자 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 관심 기업을 등록한 유저를 참조
    // Lazy 로딩을 사용하여 필요한 경우에만 유저 정보를 가져옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 관심 기업(Company)을 참조
    // Lazy 로딩을 사용하여 필요한 경우에만 회사 정보를 가져옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyId", nullable = false)
    private Company company;

    // 엔티티 생성 시 자동으로 현재 시간이 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 수정 시 자동으로 현재 시간으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}