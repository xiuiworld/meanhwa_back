package com.example.meanhwa_back.auth.service;

import com.example.meanhwa_back.auth.domain.OAuthProvider;

/**
 * 외부 OAuth provider 응답을 내부 사용자 생성에 필요한 공통 형식으로 정규화한 값 객체.
 * 카카오와 네이버의 서로 다른 JSON 구조를 AuthService가 동일하게 다룰 수 있게 한다.
 */
public record OAuthProfile(
        OAuthProvider provider,
        String oauthId,
        String email,
        String nickname
) {
}
