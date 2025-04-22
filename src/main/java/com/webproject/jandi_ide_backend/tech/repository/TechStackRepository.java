package com.webproject.jandi_ide_backend.tech.repository;

import com.webproject.jandi_ide_backend.tech.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    Optional<TechStack> findByName(String techStackName);
}
