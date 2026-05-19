package com.example.meanhwa_back.message;

import java.nio.charset.StandardCharsets;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
/**
 * 메시지 생성 인증·Rate Limit API 통합 테스트.
 * max-requests=2로 낮춰 3번째 요청에서 429를 검증한다.
 */
@TestPropertySource(properties = "app.message.rate-limit.max-requests=2")
class MessageGenerationRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generateMessageReturnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/messages/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageGenerateBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void generateMessageReturnsRateLimitErrorAfterQuotaExceeded() throws Exception {
        TokenPair tokenPair = login("rate-limit-user", "ROLE_USER");

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/messages/generate")
                            .header("Authorization", bearer(tokenPair.accessToken()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(messageGenerateBody()))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/messages/generate")
                        .header("Authorization", bearer(tokenPair.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageGenerateBody()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.errorCode").value("MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED"));
    }

    private String messageGenerateBody() {
        return """
                {
                  "flowerId": 1,
                  "selectedTagIds": [5, 9],
                  "senderName": "민수",
                  "receiverName": "지은"
                }
                """;
    }

    private TokenPair login(String oauthId, String role) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login/dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devLoginBody(oauthId, role)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return new TokenPair(
                JsonPath.read(response, "$.data.accessToken"),
                JsonPath.read(response, "$.data.refreshToken")
        );
    }

    private String devLoginBody(String oauthId, String role) {
        return """
                {
                  "oauthId": "%s",
                  "email": "%s@example.com",
                  "nickname": "민화유저",
                  "role": "%s"
                }
                """.formatted(oauthId, oauthId, role);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }
}
