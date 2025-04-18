package com.webproject.jandi_ide_backend.user.repository;

import com.webproject.jandi_ide_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGithubId(String githubId);

    Optional<User> findById(Integer id);
}
