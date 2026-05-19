package com.example.meanhwa_back.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 스토리지(S3·업로드 제한) 설정 프로퍼티 활성화. */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {
}
