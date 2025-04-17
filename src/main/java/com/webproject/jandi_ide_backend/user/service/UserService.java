package com.webproject.jandi_ide_backend.user.service;

import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.project.repository.ProjectRepository;
import com.webproject.jandi_ide_backend.security.JwtTokenProvider;
import com.webproject.jandi_ide_backend.security.TokenInfo;
import com.webproject.jandi_ide_backend.user.dto.*;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    /**
     * 깃헙 로그인 시, 액세스 토큰을 발급받고, 사용자 정보를 가져옵니다.
     * DB에 해당 유저 정보가 없다면, DB에 저장합니다.
     * @param authRequestDTO: 깃헙에서 받은 code
     * @return AuthResponseDTO
     */
    public AuthResponseDTO login(AuthRequestDTO authRequestDTO) {
        ResponseEntity<Map> response;
        try{
            String code = authRequestDTO.getCode();
            String tokenUrl = "https://github.com/login/oauth/access_token";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", githubClientId);
            params.add("client_secret", githubClientSecret);
            params.add("code", code);

            log.info("code:{}",code);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

             response = restTemplate.postForEntity(
                    tokenUrl,
                    new HttpEntity<>(params, headers),
                    Map.class
            );
        } catch (Exception e) {
            log.info("error in access_token:{}",e);
            throw new CustomException(CustomErrorCodes.GITHUB_API_FAILED);
        }


        log.info("hit response:{}",response);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            String accessToken = (String) body.get("access_token");
            log.info("accessToken:{}",accessToken);

            UserInfoDTO userInfo;
            try{
                userInfo = getUserInfo(accessToken);
            } catch (Exception e) {
                throw new RuntimeException("error getUserInfo" + e);
            }

            String githubId = userInfo.getGithubId();
            // 해당 githubId가 DB에 존재하지 않는다면, 저장 해야합니다.
            Optional<User> optionalUser = userRepository.findByGithubId(githubId);
            if(optionalUser.isEmpty()){
                // 유저가 없다면 DB에 저장합니다.
                User newUser = new User();
                newUser.setGithubId(githubId);
                newUser.setProfileImage(userInfo.getProfileImage());
                newUser.setNickname(userInfo.getNickname());
                newUser.setEmail(userInfo.getEmail());
                newUser.setGithubUsername(userInfo.getNickname());
                newUser.setRole(User.UserRole.USER);

                try{
                    userRepository.save(newUser);
                }catch (Exception e){
                    log.error("Error saving user: {}", e.getMessage());
                    throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
                }
            }

            String jwtAccessToken = jwtTokenProvider.createAccessToken(githubId, accessToken);
            String jwtRefreshToken = jwtTokenProvider.createRefreshToken(githubId, accessToken);
            return new AuthResponseDTO(jwtAccessToken,jwtRefreshToken);
        } else {
            throw new CustomException(CustomErrorCodes.GITHUB_LOGIN_FAILED);
        }
    }

    /**
     * 리프레시 토큰을 통해 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     * @param refreshToken: 리프레시 토큰
     * @return AuthResponseDTO
     */
    public AuthResponseDTO refreshToken(String refreshToken) {
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(refreshToken);
        String githubId = tokenInfo.getGithubId();
        String githubToken = tokenInfo.getGithubToken();

        // 새로운 액세스 토큰과 리프레시 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(githubId, githubToken);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(githubId, githubToken);

        return new AuthResponseDTO(newAccessToken, newRefreshToken);
    }

    /**
     * accessToken 으로 깃헙 사용자 정보 가져오기
     * DB에 해당 유저 정보가 없다면, DB에 저장합니다.
     * @param accessToken: 깃헙에서 받은 access_token
     * @return UserInfoDTO
     */
    public UserInfoDTO getUserInfo(String accessToken) {
        try{
            String userInfoUrl = "https://api.github.com/user";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            log.info("accessToken:{}",accessToken);
            headers.setBearerAuth(accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> userInfoMap = response.getBody();
            log.info("userInfoMap:{}",userInfoMap);

            String profileImage = (String) userInfoMap.get("avatar_url");
            String email = userInfoMap.get("email") == null ? "null" : (String) userInfoMap.get("email");
            String githubId = String.valueOf(userInfoMap.get("id"));
            String nickname = (String) userInfoMap.get("login");

            return new UserInfoDTO(profileImage, email, githubId, nickname);
        } catch (Exception e) {
            throw new RuntimeException("Error getUserInfo In" + e);
        }
    }

    /**
     * 자신의 깃헙 레포지토리 정보 가져오기
     * @param accessToken: header 로 받은 accessToken
     * @param id: 유저 id
     */
    public UserRepoDTO[] getUserRepos(String accessToken,Long id){
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);
        String githubId = tokenInfo.getGithubId();
        String githubToken = tokenInfo.getGithubToken();

        // 1. 유저 정보를 가져옵니다.
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 2. 자신의 정보 인지 확인합니다.
        if (!id.equals(user.getId().longValue())) {
            throw new CustomException(CustomErrorCodes.PERMISSION_DENIED);
        }

        // 3. 깃헙 API를 통해 유저의 레포지토리 정보를 가져옵니다.
        String reposUrl = String.format("https://api.github.com/users/%s/repos", user.getGithubUsername());
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map[]> response = restTemplate.exchange(
                reposUrl,
                HttpMethod.GET,
                request,
                Map[].class
        );

        Map[] repos = response.getBody();

        // 4. 레포지토리 정보에서 필요한 데이터만 추출하여 DTO로 변환합니다.
        UserRepoDTO[] userRepoDTOs = Arrays.stream(repos)
                .filter(repo -> !(Boolean) repo.get("private")) // 공개 레포지토리만 필터링
                .map(repo -> {
                    UserRepoDTO dto = new UserRepoDTO();
                    dto.setName((String) repo.get("name"));
                    dto.setDescription((String) repo.get("description"));
                    dto.setHtmlUrl((String) repo.get("html_url"));

                    Map<String, Object> ownerMap = (Map<String, Object>) repo.get("owner");
                    dto.setOwner((String) ownerMap.get("login"));

                    // GitHub API에서 제공하는 updated_at 문자열을 LocalDateTime으로 변환
                    String updatedAtStr = (String) repo.get("updated_at");
                    if (updatedAtStr != null) {
                        // GitHub API의 날짜 형식(ISO-8601)을 LocalDateTime으로 변환
                        LocalDateTime updatedAt = LocalDateTime.parse(updatedAtStr.replace("Z", ""));
                        dto.setGithubUpdatedAt(updatedAt);
                    }

                    return dto;
                })
                .toArray(UserRepoDTO[]::new);

        log.info("repos: {}", userRepoDTOs);

        return userRepoDTOs;
    }

    /**
     * 내 프로필 정보 가져오기
     * @param accessToken header로 받은 accessToken
     * @return UserResponseDTO
     */
    public UserResponseDTO getMe(String accessToken) {
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);

        // 1. accessToken 으로 githubId 만 확인
        String githubId = tokenInfo.getGithubId();

        // 2. 우리 DB 에서 유저 정보 조회
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 3. DTO 변환
        return convertToDto(user);
    }

    /**
     * 내 정보 업데이트
     * @param accessToken header 로 받은 accessToken
     * @param id: 유저 id
     * @param userUpdateDTO: 유저 정보 업데이트 DTO
     * @return UserResponseDTO
     */
    public UserResponseDTO updateUser(String accessToken, Long id, UserUpdateDTO userUpdateDTO) {
        TokenInfo tokenInfo = jwtTokenProvider.decodeToken(accessToken);
        String githubId = tokenInfo.getGithubId();

        // 1. 유저 정보를 가져옵니다.
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 2. 자신의 정보 인지 확인합니다.
        if (!id.equals(user.getId().longValue())) {
            throw new CustomException(CustomErrorCodes.PERMISSION_DENIED);
        }

        // 3. 유저 정보 업데이트
        user.setProfileImage(userUpdateDTO.getProfileImage());
        user.setEmail(userUpdateDTO.getEmail());
        user.setIntroduction(userUpdateDTO.getIntroduction());
        user.setNickname(userUpdateDTO.getNickname());

        // 4. DB에 저장
        try{
            userRepository.save(user);
        }catch (Exception e){
            log.error("Error saving user: {}", e.getMessage());
        }

        // 5. DTO 변환
        return convertToDto(user);
    }

    /**
     * 특정 유저 정보 가져오기
     * @param accessToken header로 받은 accessToken
     * @param id: 유저 id
     * @return UserResponseDTO
     */
    public UserResponseDTO getUser(String accessToken, Long id) {
        // 1. accessToken 을 검증합니다.
        jwtTokenProvider.decodeToken(accessToken);

        // 2. 해당 id의 유저 정보를 가져옵니다.
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCodes.USER_NOT_FOUND));

        // 3. DTO 변환
        return convertToDto(user);
    }

    /**
     * User 엔티티를 UserResponseDTO로 변환합니다.
     * @param user 변환할 User 엔티티
     * @return 변환된 UserResponseDTO 객체
     */
    private UserResponseDTO convertToDto(User user) {
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(user.getId());
        userResponse.setGithubId(user.getGithubId());
        userResponse.setEmail(user.getEmail());
        userResponse.setNickName(user.getNickname());
        userResponse.setProfileImage(user.getProfileImage());
        userResponse.setIntroduction(user.getIntroduction());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        userResponse.setGithubUsername(user.getGithubUsername());
        userResponse.setRole(user.getRole());

        return userResponse;
    }
}
