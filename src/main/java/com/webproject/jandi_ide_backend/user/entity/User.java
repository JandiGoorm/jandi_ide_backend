package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 사용자(User) 정보를 표현하는 엔티티 클래스
 * 깃허브 계정을 통한 로그인, 프로필 이미지, 자기소개, 닉네임, 이메일 등의 정보를 관리
 * 선호 언어와 관심 기업은 별도의 다대다 관계 조인 테이블(UserTechStack, UserFavoriteCompany)을 통해 관리
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    // 고유 식별자 (AUTO_INCREMENT)
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

    // 사용자의 이메일 주소를 저장 (유일해야 함)
    @Column(nullable = false, unique = true)
    private String email;

    // 엔티티 생성 시 자동으로 현재 시각이 기록 (수정 불가능)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 업데이트 시 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}