package com.webproject.jandi_ide_backend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 사용자(User) 정보를 표현하는 엔티티 클래스
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // GitHub 계정 아이디 (로그인에 사용되며, 중복 없이 유일해야 함)
    @Column(nullable = false, unique = true)
    private String githubId;

    // 프로필 사진의 URL을 저장
    private String profileImage;

    // 사용자의 자기소개를 저장
    // TEXT 타입으로 긴 내용을 저장할 수 있도록 설정되어 있음
    @Column(columnDefinition = "TEXT")
    private String introduction;

    // 사용자의 닉네임을 저장 (기본 닉네임 제공 가능)
    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // 사용자의 Github username 을 저장
    @Column(nullable = false)
    private String githubUsername;

    // 사용자의 이메일 주소를 저장 (유일해야 함)
    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFavoriteCompany> favoriteCompanies = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum UserRole{
        ADMIN,USER,STAFF
    }
}
