package com.webproject.jandi_ide_backend.project.service;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.project.dto.*;
import com.webproject.jandi_ide_backend.project.entity.Project;
import com.webproject.jandi_ide_backend.project.repository.ProjectRepository;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 특정 유저의 대표 프로젝트 조회
     * @param token: 사용자 액세스 토큰
     * @param id: 사용자 ID
     * @param page: 페이지
     * @param size: 페이지당 갯수
     * @return ProjectPageResponseDTO
     */
    public ProjectPageResponseDTO getProjects(String token , Integer id,Integer page, Integer size) {
        // 1. 토큰 검증
        jwtTokenProvider.decodeToken(token.replace("Bearer ", "").trim());

        // 2. 사용자 정보 조회
        User user = userRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 3. 프로젝트 정보 조회
        List<Project> projects = projectRepository.findByOwner_Id(user.getId());
        long totalItems = projectRepository.countByOwner_Id(user.getId());
        int totalPages = (int)Math.ceil((double)totalItems/size);

        if(page < 0 || page >= totalPages){
            throw new CustomException(CustomErrorCodes.INVALID_PAGE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage = projectRepository.findByOwner_Id(user.getId(), pageable);

        List<ProjectResponseDTO> projectDTOs = projectPage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        ProjectPageResponseDTO responseDTO = new ProjectPageResponseDTO();
        responseDTO.setData(projectDTOs);
        responseDTO.setTotalItems(projectPage.getTotalElements());
        responseDTO.setTotalPages(projectPage.getTotalPages());
        responseDTO.setCurrentPage(projectPage.getSize());
        responseDTO.setSize(projectPage.getSize());

        return responseDTO;
    }

    /**
     * 특정 유저의 레포지토리 트리 조회
     * @param token: 사용자 액세스 토큰
     * @param id: 프로젝트 id
     * @return 레포지토리 트리 정보
     */
    public Map<String,Object> getProject(String token,Integer id) {
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(token.replace("Bearer ", "").trim());
        String githubToken = tokenInfo.getGithubToken();

        Project project = projectRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.PROJECT_NOT_FOUND));
        String githubUsername = project.getOwner().getGithubUsername();
        String githubReponame = project.getGithubName();
        String defaultBranch = getDefaultBranch(githubToken, githubUsername, githubReponame);
        log.info("Default branch: {}", defaultBranch);


        Object repoTrees = getRepoTrees(defaultBranch,githubUsername,githubReponame,githubToken);
        Map<String, Object> result = new HashMap<>();
        result.put("name", githubReponame);
        result.put("treeData", repoTrees);

        return result;
    }

    /**
     * 내 대표 프로젝트 추가
     * @param token: 사용자 액세스 토큰
     * @param requestDTO: 프로젝트 요청 DTO
     * @return ProjectResponseDTO
     */
    public ProjectResponseDTO postProject(String token,ProjectCreateRequestDTO requestDTO){
        // 1. 토큰 검증
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(token.replace("Bearer ", "").trim());
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
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        // 5. 응답 DTO 반환
        return convertToDTO(project);
    }

    /**
     * 내 대표 프로젝트 수정
     * @param token: 사용자 액세스 토큰
     * @param id: 프로젝트 ID
     * @param requestDTO: 프로젝트 요청 DTO
     * @return ProjectResponseDTO
     */
    public ProjectResponseDTO updateProject(String token,Integer id,ProjectUpdateRequestDTO requestDTO){
        // 1. 토큰 검증
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(token.replace("Bearer ", "").trim());
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
            throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
        }

        // 6. 응답 DTO 반환
        return convertToDTO(project);
    }

    /**
     * 내 대표 프로젝트 삭제
     * @param token: 사용자 액세스 토큰
     * @param id: 프로젝트 ID
     */
    public void deleteProject(String token,Integer id){
        // 1. 토큰 검증
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(token.replace("Bearer ", "").trim());
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

    /**
     *
     * @param token: 사용자 액세스 토큰
     * @param id: 프로젝트 id
     * @param sha: sha값
     * @return BlobResponseDTO
     */
    public BlobResponseDTO getProjectBlob(String token,Integer id, String sha){
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(token.replace("Bearer ", "").trim());
        String githubId = tokenInfo.getGithubId();

        User user = userRepository.findByGithubId(githubId).orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));
        Project project = projectRepository.findById(id).orElseThrow(() -> new CustomException(CustomErrorCodes.PROJECT_NOT_FOUND));

        // 1. 레포지토리의 default branch 조회
        String githubUsername = user.getGithubUsername();
        String githubReponame = project.getGithubName();
        String githubToken = tokenInfo.getGithubToken();

        // 2. 레포지토리 트리 조회
        return getRepoBlob(sha, githubUsername, githubReponame, githubToken);
    }

    /**
     * 레포지토리의 default branch 조회
     * @param githubToken: 깃허브 토큰
     * @param githubUsername: 깃허브 사용자 이름
     * @param githubReponame: 깃허브 레포지토리 이름
     * @return default branch 이름
     */
    private String getDefaultBranch(String githubToken, String githubUsername, String githubReponame) {
        String repoUrl = "https://api.github.com/repos/" + githubUsername + "/" + githubReponame;
        log.info("repoUrl:{}",repoUrl);
        log.info("githubToken:{}",githubToken);
        // 1. 해당 레포 특정 조회 (default branch 를 가져옴)
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<Void> request = new HttpEntity(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    repoUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            return (String) responseBody.get("default_branch");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching default branch: " + e.getMessage());
        }
    }

    /**
     * 레포지토리의 트리 조회
     * @param branch: default 브랜치 이름
     * @param githubUsername: 깃허브 사용자 이름
     * @param githubReponame: 깃허브 레포지토리 이름
     * @return 레포지토리 트리 정보
     */
    private Object getRepoTrees(String branch, String githubUsername, String githubReponame,String githubToken) {
        String branchUrl = UriComponentsBuilder
                .fromHttpUrl("https://api.github.com/repos/" + githubUsername + "/" + githubReponame + "/git/trees/" + branch)
                .queryParam("recursive", "1")
                .build()
                .toUriString();
        log.info("branchUrl:{}", branchUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                branchUrl,
                HttpMethod.GET,
                request,
                Map.class
        );
        Map<String, Object> responseBody = response.getBody();
        // 최상단 URL만 웹 인터페이스 링크로 변환
        if (responseBody != null && responseBody.containsKey("url")) {
            String webUrl = "https://github.com/" + githubUsername + "/" + githubReponame;

            // 기존 url 대신 웹 인터페이스 URL로 대체
            responseBody.put("url", webUrl);
        }
        return response.getBody();
    }

    private BlobResponseDTO getRepoBlob(String sha, String githubUsername, String githubReponame,String githubToken) {
        String blobUrl = "https://api.github.com/repos/" + githubUsername + "/" + githubReponame + "/git/blobs/" + sha;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<BlobResponseDTO> response = restTemplate.exchange(
                blobUrl,
                HttpMethod.GET,
                request,
                BlobResponseDTO.class
        );

        return response.getBody();
    }

    private ProjectResponseDTO convertToDTO(Project project) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setGithubName(project.getGithubName());
        dto.setDescription(project.getDescription());
        dto.setUrl(project.getUrl());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }
}
