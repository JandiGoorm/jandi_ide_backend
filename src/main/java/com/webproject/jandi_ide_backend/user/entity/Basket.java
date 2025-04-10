package com.webproject.jandi_ide_backend.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 코딩 테스트 "문제집"을 표현하는 엔티티 클래스
 * 각 문제집은 한 명의 사용자(User)에 소속되며, 사용자가 직접 생성하고 관리할 수 있음
 */
@Entity
@Table(name = "Basket")
@Getter
@Setter
@NoArgsConstructor
public class Basket {

    // 고유 식별자: AUTO_INCREMENT를 사용하여 값이 자동 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 문제집 소유자: 해당 문제집을 생성한 사용자(User)를 참조
    // Lazy 로딩을 사용하여 실제로 필요할 때만 데이터를 로드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 문제집의 이름: 문제집을 식별하기 위한 이름을 저장
    // 이 값은 반드시 입력되어야 함
    @Column(nullable = false)
    private String name;

    // 코딩 테스트에 사용할 타이머 시간: 문제집에 설정된 시간을 초 단위로 저장
    // 선택 항목으로, 사용자가 원하는 경우에만 입력할 수 있음
    private Integer timer;

    // 코딩 테스트 언어: 예를 들어, Java, Python 등 문제집에서 진행할 코딩 테스트의 언어를 저장
    private String language;

    // 엔티티가 생성될 때 자동으로 현재 시각이 기록 (변경 불가)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티가 수정될 때마다 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
