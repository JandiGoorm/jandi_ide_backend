package com.webproject.jandi_ide_backend.user.service;

import com.webproject.jandi_ide_backend.user.dto.UserInfoDTO;
import com.webproject.jandi_ide_backend.user.dto.UserLoginDTO;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Optional;
import java.util.Map;

@Slf4j
@Service
public class UserService {
    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserLoginDTO getToken(String code) {
        String tokenUrl = "https://github.com/login/oauth/access_token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", githubClientId);
        params.add("client_secret", githubClientSecret);
        params.add("code", code);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl,
                new HttpEntity<>(params, headers),
                Map.class
        );


        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            String accessToken = (String) body.get("access_token");

            // 여기서 해당 유저의 정보를 가져옵니다.
            // 필요한 값은 githubId, nickname , email , profileImage 입니다.
            UserInfoDTO userInfo = getUserInfo(accessToken);
            log.info("userInfo: {}", userInfo);
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

                try{
                    userRepository.save(newUser);
                }catch (Exception e){
                    log.error("Error saving user: {}", e.getMessage());
                }
            }

            return new UserLoginDTO(accessToken);
        } else {
            throw new RuntimeException("깃헙 토큰 발급 실패: " + response.getStatusCode());
        }
    }

    public UserInfoDTO getUserInfo(String accessToken) {
        String userInfoUrl = "https://api.github.com/user";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
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
        log.info("GitHub user info: {}", userInfoMap);

        String profileImage = (String) userInfoMap.get("avatar_url");
        String email = (String) userInfoMap.get("email");
        String githubId = String.valueOf(userInfoMap.get("id"));
        String nickname = (String) userInfoMap.get("login");

        return new UserInfoDTO(profileImage, email, githubId, nickname);
    }
}
