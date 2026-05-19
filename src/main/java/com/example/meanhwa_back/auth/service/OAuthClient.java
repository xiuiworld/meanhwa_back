package com.example.meanhwa_back.auth.service;

import com.example.meanhwa_back.auth.domain.OAuthProvider;

/** OAuth access token으로 사용자 프로필을 조회하는 클라이언트. */
public interface OAuthClient {
    OAuthProvider getProvider();

    OAuthProfile fetchProfile(String accessToken);
}
