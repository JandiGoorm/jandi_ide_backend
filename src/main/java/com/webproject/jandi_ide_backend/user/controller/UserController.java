package com.webproject.jandi_ide_backend.user.controller;

import com.webproject.jandi_ide_backend.user.dto.UserLoginDTO;
import com.webproject.jandi_ide_backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API를 처리하는 컨트롤러.
 * 사용자 로그인, 정보 조회 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?>register(@RequestBody String code){
        // 1) code가 없으면 에러
        if(code == null || code.isBlank()){
            return ResponseEntity.badRequest().body("코드가 유효하지 않습니다.");
        }

        // 2) 깃헙 OAuth 로그인 처리 (깃헙에 토큰 요청 -> accessToken 발급 ->  DB 저장/조회)
        UserLoginDTO loginResp;
        try {
            loginResp = userService.getToken(code);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("깃헙 로그인 처리 실패: " + e.getMessage());
        }

        // 3) 최종적으로 토큰 반환
        return ResponseEntity.ok(loginResp);
    }
}
