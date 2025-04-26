package com.webproject.jandi_ide_backend.user.service;

import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.user.dto.RespFavoriteCompanyDTO;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.entity.UserFavoriteCompany;
import com.webproject.jandi_ide_backend.user.repository.UserFavoriteCompanyRepository;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteCompanyService {
    private final UserRepository userRepository;
    private final UserFavoriteCompanyRepository userFavoriteCompanyRepository;
    private final CompanyRepository companyRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 인증 토큰에서 GitHub ID를 추출합니다.
     * @param token 인증 토큰
     * @return GitHub ID
     */
    public String getGithubIdFromToken(String token) {
        // accessToken 얻기
        if (token == null || token.isBlank() || !token.startsWith("Bearer "))
            throw new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN);
        String accessToken = token.replace("Bearer ", "").trim();

        // 토큰 디코딩 및 깃헙 아이디 추출
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);
        return tokenInfo.getGithubId();
    }

    // 전체 선호 기업 리스트 조회
    public List<RespFavoriteCompanyDTO> readFavoriteCompany(String githubId) {
        // 유저 검증
        User user = findUserByGithubId(githubId);

        List<UserFavoriteCompany> favoriteCompanyList = userFavoriteCompanyRepository.findAllByUser(user);
        return favoriteCompanyList.stream()
                .map(favoriteCompany -> new RespFavoriteCompanyDTO(favoriteCompany.getCompany()))
                .toList();
    }

    /**
     * 인증 토큰으로 사용자의 선호 기업 목록을 조회합니다.
     * @param token 인증 토큰
     * @return 선호 기업 목록
     */
    public List<RespFavoriteCompanyDTO> readFavoriteCompanyByToken(String token) {
        String githubId = getGithubIdFromToken(token);
        return readFavoriteCompany(githubId);
    }

    // 선호 기업 추가 - 첫 로그인용
    public List<RespFavoriteCompanyDTO> postFavoriteCompany(String githubId, List<String> companyNameList) {
        // 유저 검증
        User user = findUserByGithubId(githubId);

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

    /**
     * 인증 토큰으로 선호 기업 목록을 설정합니다.
     * @param token 인증 토큰
     * @param companyNameList 기업 이름 목록
     * @return 선호 기업 목록
     */
    public List<RespFavoriteCompanyDTO> postFavoriteCompanyByToken(String token, List<String> companyNameList) {
        if (companyNameList == null || companyNameList.isEmpty()) {
            throw new RuntimeException("선택된 기업이 없습니다");
        }
        String githubId = getGithubIdFromToken(token);
        return postFavoriteCompany(githubId, companyNameList);
    }

    public boolean putFavoriteCompany(String githubId, Integer companyId) {
        // 유저 및 회사 검증
        User user = findUserByGithubId(githubId);
        Company company = findCompanyById(companyId);

        if (userFavoriteCompanyRepository.findByUserIdAndCompanyId(user.getId(), companyId).isPresent()) {
            throw new RuntimeException("이미 선호기업으로 등록되어 있습니다.");
        }

        createData(user, company);
        return true;
    }

    /**
     * 인증 토큰으로 특정 기업을 선호 목록에 추가합니다.
     * @param token 인증 토큰
     * @param companyId 기업 ID
     * @return 추가 성공 여부
     */
    public boolean putFavoriteCompanyByToken(String token, Integer companyId) {
        String githubId = getGithubIdFromToken(token);
        return putFavoriteCompany(githubId, companyId);
    }

    public boolean deleteFavoriteCompany(String githubId, Integer companyId) {
        // 유저 및 회사 검증
        User user = findUserByGithubId(githubId);
        Company company = findCompanyById(companyId);

        UserFavoriteCompany userFavoriteCompany = userFavoriteCompanyRepository.findByUserIdAndCompanyId(user.getId(), companyId)
                .orElseThrow(() -> new RuntimeException("선호기업에 등록되지 않은 기업입니다."));
        userFavoriteCompanyRepository.delete(userFavoriteCompany);
        return true;
    }

    /**
     * 인증 토큰으로 특정 기업을 선호 목록에서 삭제합니다.
     * @param token 인증 토큰
     * @param companyId 기업 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteFavoriteCompanyByToken(String token, Integer companyId) {
        String githubId = getGithubIdFromToken(token);
        return deleteFavoriteCompany(githubId, companyId);
    }

    /// 존재 여부를 검증할 함수
    private User findUserByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
    }

    private Company findCompanyById(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(()-> new RuntimeException("해당 회사가 존재하지 않습니다"));
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
