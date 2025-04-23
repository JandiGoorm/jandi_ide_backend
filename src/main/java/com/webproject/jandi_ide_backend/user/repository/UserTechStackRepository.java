package com.webproject.jandi_ide_backend.user.repository;

import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.entity.UserTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTechStackRepository extends JpaRepository<UserTechStack, Long> {
    List<UserTechStack> findAllByUser(User user);
}
