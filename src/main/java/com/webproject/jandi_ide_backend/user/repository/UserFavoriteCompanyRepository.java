package com.webproject.jandi_ide_backend.user.repository;

import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.entity.UserFavoriteCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteCompanyRepository extends JpaRepository<UserFavoriteCompany, Integer> {

    List<UserFavoriteCompany> findAllByUser(User user);
    Optional<UserFavoriteCompany> findByUserIdAndCompanyId(Integer userId, Integer companyId);
}
