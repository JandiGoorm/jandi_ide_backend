package com.webproject.jandi_ide_backend.project.service;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.project.dto.ProjectCreateRequestDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectResponseDTO;
import com.webproject.jandi_ide_backend.project.dto.ProjectUpdateRequestDTO;
import com.webproject.jandi_ide_backend.project.entity.Project;
import com.webproject.jandi_ide_backend.project.repository.ProjectRepository;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProjectService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public ProjectService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository,
                          ProjectRepository projectRepository)  {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * 대표 프로젝트 추가
     * @param accessToken: 사용자 액세스 토큰
//     * @param id: 사용자 ID
     * @param requestDTO: 프로젝트 요청 DTO
     * @return ProjectResponseDTO
     */
    public ProjectResponseDTO postProject(String accessToken, ProjectCreateRequestDTO requestDTO){
        // 1. 토큰 검증
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);
        String githubId = tokenInfo.getGithubId();

        // 2. 사용자 정보 조회
        User user = userRepository.findByGithubId(githubId).orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 3. 프로젝트 정보 저장
        Project project = new Project();
        project.setName(requestDTO.getName());
        project.setGithubName(requestDTO.getGithubName());
        project.setDescription(requestDTO.getDescription());
        project.setUrl(requestDTO.getUrl());
        project.setOwner(user);

        // 4. 프로젝트 저장
        try{
            projectRepository.save(project);
        } catch (Exception e) {
            log.error("Error saving project: {}", e.getMessage());
        }

        // 5. 응답 DTO 반환
        return convertToDTO(project);
    }

    /**
     * 대표 프로젝트 수정
     * @param accessToken: 사용자 액세스 토큰
     * @param id: 프로젝트 ID
     * @param requestDTO: 프로젝트 요청 DTO
     * @return ProjectResponseDTO
     */
    public ProjectResponseDTO updateProject(String accessToken,Integer id,ProjectUpdateRequestDTO requestDTO){
        // 1. 토큰 검증
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);
        String githubId = tokenInfo.getGithubId();

        // 2. 사용자 정보 조회 및 프로젝트 조회
        User user = userRepository.findByGithubId(githubId).orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));
        Project project = projectRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.PROJECT_NOT_FOUND));

        // 3. 자신의 프로젝트가 맞는지 확인
        if(!project.getOwner().getId().equals(user.getId())){
            throw new CustomException(CustomErrorCodes.PERMISSION_DENIED);
        }

        // 4. 프로젝트 정보 업데이트
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());

        // 5. 프로젝트 저장
        try{
            projectRepository.save(project);
        } catch (Exception e) {
            log.error("Error saving project: {}", e.getMessage());
        }

        // 6. 응답 DTO 반환
        return convertToDTO(project);
    }

    public void deleteProject(String accessToken,Integer id){
        // 1. 토큰 검증
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);
        String githubId = tokenInfo.getGithubId();

        // 2. 사용자 정보 조회 및 프로젝트 조회
        User user = userRepository.findByGithubId(githubId).orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));
        Project project = projectRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.PROJECT_NOT_FOUND));

        // 3. 자신의 프로젝트가 맞는지 확인
        if(!project.getOwner().getId().equals(user.getId())){
            throw new CustomException(CustomErrorCodes.PERMISSION_DENIED);
        }

        // 4. 프로젝트 삭제
        try{
            projectRepository.delete(project);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }
    }


    private ProjectResponseDTO convertToDTO(Project project) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setGithubName(project.getGithubName());
        dto.setDescription(project.getDescription());
        dto.setUrl(project.getUrl());
        dto.setOwner(project.getOwner());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }
}
