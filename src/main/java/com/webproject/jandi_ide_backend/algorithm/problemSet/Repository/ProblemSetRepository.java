package com.webproject.jandi_ide_backend.algorithm.problemSet.Repository;

import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {
}
