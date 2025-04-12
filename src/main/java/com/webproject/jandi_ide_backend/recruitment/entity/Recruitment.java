package com.webproject.jandi_ide_backend.recruitment.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.webproject.jandi_ide_backend.company.entity.Company;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 채용 공고 정보를 표현하는 엔티티 클래스
 * 각 채용 공고는 특정 회사(Company)에 속하며, 제목, 상세 설명, 시작 날짜, 종료 날짜 등의 정보를 포함
 */
@Entity
@Table(name = "Recruitment")
@Getter
@Setter
@NoArgsConstructor
public class Recruitment {

    // 고유 식별자 (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 해당 채용 공고가 소속된 회사를 참조
    // Lazy Fetching을 사용하여 실제로 회사 정보가 필요한 경우에만 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyId", nullable = false)
    private Company company;

    // 채용 공고의 제목 (반드시 입력되어야 함)
    @Column(nullable = false)
    private String title;

    // 채용 공고의 상세 설명을 저장 (긴 텍스트를 저장할 수 있도록 TEXT 타입 사용)
    @Column(columnDefinition = "TEXT")
    private String description;

    // 채용 시작 날짜 (반드시 입력되어야 함)
    @Column(nullable = false)
    private LocalDateTime startDate;

    // 채용 종료 날짜 (반드시 입력되어야 함)
    @Column(nullable = false)
    private LocalDateTime endDate;

    // 엔티티 생성 시 자동으로 현재 시각이 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 수정 시 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
