package com.webproject.jandi_ide_backend.submission.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.algorithm.entity.Problem;

/**
 * 코딩 테스트 문제에 대한 제출 기록을 저장하는 엔티티
 * 이 엔티티는 사용자가 문제를 제출한 횟수, 정답 여부, 그리고 문제 해결에 걸린 시간을 기록
 */
@Entity
@Table(name = "Submission")
@Getter
@Setter
@NoArgsConstructor
public class Submission {

    // 고유 식별자 (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 제출을 진행한 사용자를 참조
    // Lazy 로딩을 사용하여 실제 사용 시에만 데이터를 불러옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 제출된 문제를 참조
    // Lazy 로딩을 사용하여 필요한 경우에만 문제 데이터를 로드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problemId", nullable = false)
    private Problem problem;

    // 제출 기록에서 해당 제출이 문제를 맞혔는지 여부를 저장
    @Column(nullable = false)
    private boolean solved;

    // 정답을 맞추기까지 시도한 횟수를 저장 (최초 시도 포함)
    @Column(nullable = false)
    private int submissionCount;

    // 문제를 맞추는데 소요된 시간(초 단위)을 저장
    // 제출 실패한 경우에는 null이 될 수 있음
    private Integer timeTaken;

    // 엔티티 생성 시 자동으로 현재 시각을 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티 수정 시 자동으로 현재 시각으로 갱신
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
