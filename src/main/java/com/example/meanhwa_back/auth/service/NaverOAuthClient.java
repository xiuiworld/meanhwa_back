package com.example.meanhwa_back.auth.service;

import com.example.meanhwa_back.auth.config.OAuthProperties;
import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** 네이버 OAuth 사용자 정보 API 클라이언트. */
@Component
public class NaverOAuthClient implements OAuthClient {
    private final OAuthProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public NaverOAuthClient(OAuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(properties.getNaver().getTimeoutMillis()))
                .build();
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public OAuthProfile fetchProfile(String accessToken) {
        try {
            String response = restClient.get()
                    .uri(properties.getNaver().getUserInfoUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            return parseProfile(response);
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN);
        }
    }

    OAuthProfile parseProfile(String response) {
        try {
            JsonNode profile = objectMapper.readTree(response).path("response");
            String oauthId = textOrNull(profile.path("id"));
            if (oauthId == null) {
                throw new IllegalArgumentException("Naver profile id is missing.");
            }

            return new OAuthProfile(
                    OAuthProvider.NAVER,
                    oauthId,
                    textOrNull(profile.path("email")),
                    textOrNull(profile.path("nickname"))
            );
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN);
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        return node.asText();
    }

    private SimpleClientHttpRequestFactory requestFactory(int timeoutMillis) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return factory;
    }
}
