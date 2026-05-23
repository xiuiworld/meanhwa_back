package com.example.meanhwa_back.auth.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 카카오·네이버 사용자 정보 API URL 등 OAuth 설정. */
@Validated
@ConfigurationProperties(prefix = "app.oauth")
public class OAuthProperties {
    @Valid
    @NotNull
    private Provider kakao = new Provider("https://kapi.kakao.com/v2/user/me");

    @Valid
    @NotNull
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

    /**
     * OAuth provider별 사용자 정보 API endpoint와 네트워크 timeout 설정.
     */
    public static class Provider {
        @NotBlank
        private String userInfoUrl;

        @Min(100)
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

        /**
         * Bean Validation용 helper. OAuth 사용자 정보 endpoint는 HTTP(S) URL이어야 한다.
         */
        @AssertTrue(message = "OAuth user-info-url must be an absolute HTTP(S) URL.")
        public boolean isUserInfoUrlHttpUrl() {
            if (userInfoUrl == null || userInfoUrl.isBlank()) {
                return true;
            }
            try {
                URI uri = URI.create(userInfoUrl);
                String scheme = uri.getScheme();
                return uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
    }
}
