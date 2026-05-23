package com.example.meanhwa_back.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** application.yml의 app.jwt 설정 바인딩. */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    @NotBlank
    @Size(min = 32, message = "JWT secret은 32자 이상이어야 합니다.")
    private String secret;

    @Min(1)
    private long accessTokenValidityMinutes = 30;

    @Min(1)
    private long refreshTokenValidityDays = 14;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenValidityMinutes() {
        return accessTokenValidityMinutes;
    }

    public void setAccessTokenValidityMinutes(long accessTokenValidityMinutes) {
        this.accessTokenValidityMinutes = accessTokenValidityMinutes;
    }

    public long getRefreshTokenValidityDays() {
        return refreshTokenValidityDays;
    }

    public void setRefreshTokenValidityDays(long refreshTokenValidityDays) {
        this.refreshTokenValidityDays = refreshTokenValidityDays;
    }
}
