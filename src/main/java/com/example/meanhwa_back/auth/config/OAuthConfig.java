package com.example.meanhwa_back.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** OAuth 클라이언트 설정 프로퍼티 활성화. */
@Configuration
@EnableConfigurationProperties(OAuthProperties.class)
public class OAuthConfig {
}
