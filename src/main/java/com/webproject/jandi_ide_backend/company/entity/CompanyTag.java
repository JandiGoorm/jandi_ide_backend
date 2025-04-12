package com.webproject.jandi_ide_backend.company.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.webproject.jandi_ide_backend.tag.entity.Tag;

/**
 * 회사와 태그 간의 관계를 표현하는 엔티티
 * 각 인스턴스는 특정 회사(Company)와 특정 태그(Tag)를 연결
 * Lazy Fetching을 사용하여 실제 데이터가 필요한 시점에만 관련 객체를 로딩
 */
@Entity
@Table(name = "CompanyTag")
@Getter
@Setter
@NoArgsConstructor
public class CompanyTag {

    // 고유 식별자 (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 연결되는 회사 엔티티 (Company)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyId", nullable = false)
    private Company company;

    // 연결되는 태그 엔티티 (Tag)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagId", nullable = false)
    private Tag tag;

    // 엔티티 생성 시 자동으로 현재 시간이 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 수정 시 자동으로 현재 시간으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
