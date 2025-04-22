package com.webproject.jandi_ide_backend.user.service;

import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.user.dto.RespFavoriteCompanyDTO;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.entity.UserFavoriteCompany;
import com.webproject.jandi_ide_backend.user.repository.UserFavoriteCompanyRepository;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteCompanyService {
    private final UserRepository userRepository;
    private final UserFavoriteCompanyRepository userFavoriteCompanyRepository;
    private final CompanyRepository companyRepository;

    // 전체 선호 기업 리스트 조회
    public List<RespFavoriteCompanyDTO> readFavoriteCompany(String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        List<UserFavoriteCompany> favoriteCompanyList = userFavoriteCompanyRepository.findAllByUser(user);
        return favoriteCompanyList.stream()
                .map(favoriteCompany -> new RespFavoriteCompanyDTO(favoriteCompany.getCompany()))
                .toList();
    }

    // 선호 기업 추가 - 첫 로그인용
    public List<RespFavoriteCompanyDTO> postFavoriteCompany(String githubId, List<String> companyNameList) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 새 기업 리스트 매핑
        List<Company> newCompanyList = new ArrayList<>();
        for (String companyName : companyNameList) {
            companyRepository.findByCompanyName(companyName)
                    .ifPresent(newCompanyList::add);
        }

        if (newCompanyList.isEmpty())
            throw new RuntimeException("잘못된 기업을 선택하셨습니다.");

        // 기존 선호 기업 정보 제거 - 혹시 모르니까...?
        List<UserFavoriteCompany> favoriteCompanyList = userFavoriteCompanyRepository.findAllByUser(user);
        if (!favoriteCompanyList.isEmpty()) {
            userFavoriteCompanyRepository.deleteAll(favoriteCompanyList);
        }

        // 새 선호 기업 정보로 추가
        List<UserFavoriteCompany> newFavoriteCompanyList = new ArrayList<>();
        for (Company company : newCompanyList) {
            UserFavoriteCompany newData = createData(user, company);
            newFavoriteCompanyList.add(newData);
        }

        return newFavoriteCompanyList.stream()
                .map(favoriteCompany -> new RespFavoriteCompanyDTO(favoriteCompany.getCompany()))
                .toList();
    }

    /// 실제 DB CRUD
    private UserFavoriteCompany createData(User user, Company company) {
        UserFavoriteCompany userFavoriteCompany = new UserFavoriteCompany();
        userFavoriteCompany.setUser(user);
        userFavoriteCompany.setCompany(company);
        userFavoriteCompany.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        userFavoriteCompany.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        return userFavoriteCompanyRepository.save(userFavoriteCompany);
    }
}
