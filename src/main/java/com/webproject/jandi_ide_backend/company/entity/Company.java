package com.webproject.jandi_ide_backend.company.entity;

import com.webproject.jandi_ide_backend.jobPosting.entity.JobPosting;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 기업 엔티티
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 기업명
    @Column(name = "company_name", nullable = false)
    private String companyName;

    // 랜덤하게 줄 문제를 난이도로 배열에 저장
    @ElementCollection
    @CollectionTable(name = "company_problems", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "levels")
    private List<Integer> levels = new ArrayList<>();

    // 풀이 시간
    @Column(name = "time_in_minutes")
    private Integer timeInMinutes;

    // 해당 기업에서 코테에 사용하는 프로그래밍 언어 배열
    @ElementCollection
    @CollectionTable(name = "company_programming_languages", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "language")
    private List<String> programmingLanguages = new ArrayList<>();

    // 채용 공고와의 관계. 기업과 연결이 끊어진 채용 공고는 자동으로 삭제
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobPosting> jobPostings = new ArrayList<>();

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
