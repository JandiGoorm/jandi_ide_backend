package com.webproject.jandi_ide_backend.user.service;

import com.webproject.jandi_ide_backend.tech.entity.TechStack;
import com.webproject.jandi_ide_backend.tech.repository.TechStackRepository;
import com.webproject.jandi_ide_backend.user.dto.RespTechStackDTO;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.entity.UserTechStack;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import com.webproject.jandi_ide_backend.user.repository.UserTechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTechStackService {
    private final UserRepository userRepository;
    private final UserTechStackRepository userTechStackRepository;
    private final TechStackRepository techStackRepository;

    // 전체 선호 기업 리스트 조회
    public List<RespTechStackDTO> readFavoriteTechStack(String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        List<UserTechStack> favoriteTechStackList = userTechStackRepository.findAllByUser(user);
        return favoriteTechStackList.stream()
                .map(favoriteTechStack -> new RespTechStackDTO(favoriteTechStack.getTechStack()))
                .toList();
    }

    // 이전의 선호 기업 정보를 지우고 새 정보로 대체
    public List<RespTechStackDTO> putFavoriteTechStack(String githubId, List<String> techStackNameList) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 새 기업 리스트 매핑
        List<TechStack> newTechStackList = new ArrayList<>();
        for (String techStackName : techStackNameList) {
            techStackRepository.findByName(techStackName)
                    .ifPresent(newTechStackList::add);
        }

        if (newTechStackList.isEmpty())
            throw new RuntimeException("잘못된 언어를 선택하셨습니다.");

        // 기존 선호 기업 정보 제거
        List<UserTechStack> favoriteTechStackList = userTechStackRepository.findAllByUser(user);
        if (!favoriteTechStackList.isEmpty()) {
            userTechStackRepository.deleteAll(favoriteTechStackList);
        }

        // 새 선호 기업 정보로 추가
        List<UserTechStack> newUserTechStackList = new ArrayList<>();
        for (TechStack techStack : newTechStackList) {
            UserTechStack newData = createData(user, techStack);
            newUserTechStackList.add(newData);
        }

        return newUserTechStackList.stream()
                .map(favoriteTechStack -> new RespTechStackDTO(favoriteTechStack.getTechStack()))
                .toList();
    }

    /// 실제 DB CRUD
    private UserTechStack createData(User user, TechStack techStack) {
        UserTechStack userFavoriteTechStack = new UserTechStack();
        userFavoriteTechStack.setUser(user);
        userFavoriteTechStack.setTechStack(techStack);
        userFavoriteTechStack.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        userFavoriteTechStack.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        return userTechStackRepository.save(userFavoriteTechStack);
    }
}
