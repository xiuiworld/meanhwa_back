package com.example.meanhwa_back.message.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** OpenAI Responses API 호출 설정. API key가 비어 있으면 템플릿 생성기로 fallback한다. */
@Validated
@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {
    private String apiKey;

    @NotBlank
    private String baseUrl = "https://api.openai.com";

    @NotBlank
    private String model = "gpt-5.4-mini";

    @Min(100)
    private int timeoutMillis = 5000;

    @Min(1)
    private int maxOutputTokens = 300;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }
}
