package com.example.meanhwa_back.common.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/** API CORS 허용 origin 목록을 바인딩하고 비어 있는 운영 설정을 차단한다. */
@Validated
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    @NotEmpty
    private List<@NotBlank String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:3000",
            "http://localhost:5173"
    ));

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @AssertTrue(message = "CORS allowed-origins cannot contain '*' because credentials are enabled.")
    public boolean isWildcardOriginAbsent() {
        if (allowedOrigins == null) {
            return true;
        }
        return allowedOrigins.stream()
                .noneMatch(origin -> "*".equals(origin.trim()));
    }
}
