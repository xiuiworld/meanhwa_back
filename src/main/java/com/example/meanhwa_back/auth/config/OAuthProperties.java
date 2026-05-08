package com.example.meanhwa_back.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth")
public class OAuthProperties {
    private Provider kakao = new Provider("https://kapi.kakao.com/v2/user/me");
    private Provider naver = new Provider("https://openapi.naver.com/v1/nid/me");

    public Provider getKakao() {
        return kakao;
    }

    public void setKakao(Provider kakao) {
        this.kakao = kakao;
    }

    public Provider getNaver() {
        return naver;
    }

    public void setNaver(Provider naver) {
        this.naver = naver;
    }

    public static class Provider {
        private String userInfoUrl;
        private int timeoutMillis = 3000;

        public Provider() {
        }

        public Provider(String userInfoUrl) {
            this.userInfoUrl = userInfoUrl;
        }

        public String getUserInfoUrl() {
            return userInfoUrl;
        }

        public void setUserInfoUrl(String userInfoUrl) {
            this.userInfoUrl = userInfoUrl;
        }

        public int getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }
    }
}
