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

/** 카카오 OAuth 사용자 정보 API 클라이언트. */
@Component
public class KakaoOAuthClient implements OAuthClient {
    private final OAuthProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public KakaoOAuthClient(OAuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(properties.getKakao().getTimeoutMillis()))
                .build();
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.KAKAO;
    }

    /**
     * 외부 OAuth provider에서 사용자 profile을 가져와 내부 공통 모델로 변환한다.
     */
    @Override
    public OAuthProfile fetchProfile(String accessToken) {
        try {
            String response = restClient.get()
                    .uri(properties.getKakao().getUserInfoUrl())
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
            JsonNode root = objectMapper.readTree(response);
            JsonNode idNode = root.path("id");
            if (idNode.isMissingNode() || idNode.isNull()) {
                throw new IllegalArgumentException("Kakao profile id is missing.");
            }

            JsonNode account = root.path("kakao_account");
            String email = textOrNull(account.path("email"));
            String nickname = textOrNull(account.path("profile").path("nickname"));
            return new OAuthProfile(OAuthProvider.KAKAO, idNode.asText(), email, nickname);
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
