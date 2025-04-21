package com.webproject.jandi_ide_backend.algorithm.problemSet.Repository;

import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {
    List<ProblemSet> findAllByUser(User user); // 사용자의 모든 문제집 조회
}
