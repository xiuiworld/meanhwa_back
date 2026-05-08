package com.example.meanhwa_back.auth.service;

import com.example.meanhwa_back.auth.config.OAuthProperties;
import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.common.error.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthProfileParserTests {

    @Test
    void parsesKakaoProfileShape() {
        KakaoOAuthClient client = new KakaoOAuthClient(new OAuthProperties(), new ObjectMapper());

        OAuthProfile profile = client.parseProfile("""
                {
                  "id": 123456789,
                  "kakao_account": {
                    "email": "kakao-user@example.com",
                    "profile": {
                      "nickname": "카카오유저"
                    }
                  }
                }
                """);

        assertThat(profile.provider()).isEqualTo(OAuthProvider.KAKAO);
        assertThat(profile.oauthId()).isEqualTo("123456789");
        assertThat(profile.email()).isEqualTo("kakao-user@example.com");
        assertThat(profile.nickname()).isEqualTo("카카오유저");
    }

    @Test
    void parsesNaverProfileShape() {
        NaverOAuthClient client = new NaverOAuthClient(new OAuthProperties(), new ObjectMapper());

        OAuthProfile profile = client.parseProfile("""
                {
                  "response": {
                    "id": "naver-user-1",
                    "email": "naver-user@example.com",
                    "nickname": "네이버유저"
                  }
                }
                """);

        assertThat(profile.provider()).isEqualTo(OAuthProvider.NAVER);
        assertThat(profile.oauthId()).isEqualTo("naver-user-1");
        assertThat(profile.email()).isEqualTo("naver-user@example.com");
        assertThat(profile.nickname()).isEqualTo("네이버유저");
    }

    @Test
    void rejectsProfilesWithoutProviderId() {
        KakaoOAuthClient kakaoClient = new KakaoOAuthClient(new OAuthProperties(), new ObjectMapper());
        NaverOAuthClient naverClient = new NaverOAuthClient(new OAuthProperties(), new ObjectMapper());

        assertThatThrownBy(() -> kakaoClient.parseProfile("{\"kakao_account\":{}}"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> naverClient.parseProfile("{\"response\":{}}"))
                .isInstanceOf(BusinessException.class);
    }
}
