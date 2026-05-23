package com.example.meanhwa_back.common.config;

import java.time.Duration;
import java.util.Set;

import com.example.meanhwa_back.common.security.JwtProperties;
import com.example.meanhwa_back.message.config.MessageGenerationRateLimitProperties;
import com.example.meanhwa_back.message.config.OpenAiProperties;
import com.example.meanhwa_back.storage.config.StorageProperties;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

/** 운영 설정 누락이 애플리케이션 기동 전에 Bean Validation으로 드러나는지 검증한다. */
class ConfigurationPropertiesValidationTest {
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    void jwtSecretMustBeLongEnoughForHmacSigning() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("short");

        assertThat(propertyPaths(validator.validate(properties))).contains("secret");
    }

    @Test
    void s3StorageRequiresAllPublicRuntimeSettings() {
        StorageProperties properties = new StorageProperties();
        properties.setType("s3");

        assertThat(propertyPaths(validator.validate(properties))).contains("s3ConfiguredWhenEnabled");
    }

    @Test
    void rateLimitWindowMustBePositive() {
        MessageGenerationRateLimitProperties properties = new MessageGenerationRateLimitProperties();
        properties.setWindow(Duration.ZERO);

        assertThat(propertyPaths(validator.validate(properties))).contains("windowPositive");
    }

    @Test
    void openAiTimeoutMustBeOperationallyUsable() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setTimeoutMillis(1);

        assertThat(propertyPaths(validator.validate(properties))).contains("timeoutMillis");
    }

    private Set<String> propertyPaths(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}
