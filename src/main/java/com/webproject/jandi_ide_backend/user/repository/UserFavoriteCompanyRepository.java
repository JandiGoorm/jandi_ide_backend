package com.webproject.jandi_ide_backend.user.repository;

import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.entity.UserFavoriteCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFavoriteCompanyRepository extends JpaRepository<UserFavoriteCompany, Integer> {

    List<UserFavoriteCompany> findAllByUser(User user);
}
