package com.example.meanhwa_back.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OAuthProperties.class)
public class OAuthConfig {
}
