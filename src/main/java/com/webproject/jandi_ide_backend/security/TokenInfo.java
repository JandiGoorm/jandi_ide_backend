package com.webproject.jandi_ide_backend.security;

import lombok.Getter;

/**
 * 토큰 정보를 담는 내부 클래스
 */
@Getter
public class TokenInfo {
    private final String githubId;
    private final String githubToken;

    public TokenInfo(String githubId, String githubToken) {
        this.githubId = githubId;
        this.githubToken = githubToken;
    }
}
