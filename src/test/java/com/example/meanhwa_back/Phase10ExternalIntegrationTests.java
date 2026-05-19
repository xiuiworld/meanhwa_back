package com.example.meanhwa_back;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class Phase10ExternalIntegrationTests {
    private static HttpServer server;
    private static volatile OpenAiMode openAiMode = OpenAiMode.ERROR;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetOpenAiMode() {
        openAiMode = OpenAiMode.ERROR;
    }

    @DynamicPropertySource
    static void phase10Properties(DynamicPropertyRegistry registry) {
        startServer();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:meanhwa-phase10;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("app.oauth.kakao.user-info-url", () -> baseUrl + "/kakao/userinfo");
        registry.add("app.oauth.naver.user-info-url", () -> baseUrl + "/naver/userinfo");
        registry.add("app.openai.api-key", () -> "test-openai-key");
        registry.add("app.openai.base-url", () -> baseUrl);
        registry.add("app.openai.timeout-millis", () -> "1000");
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void kakaoLoginCreatesUserAndReturnsTokens() throws Exception {
        TokenPair tokenPair = socialLogin("kakao", "valid-kakao-token");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("KAKAO"))
                .andExpect(jsonPath("$.data.oauthId").value("123456789"))
                .andExpect(jsonPath("$.data.email").value("kakao-user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("카카오유저"));
    }

    @Test
    void naverLoginCreatesUserAndReturnsTokens() throws Exception {
        TokenPair tokenPair = socialLogin("naver", "valid-naver-token");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("NAVER"))
                .andExpect(jsonPath("$.data.oauthId").value("naver-user-1"))
                .andExpect(jsonPath("$.data.email").value("naver-user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("네이버유저"));
    }

    @Test
    void invalidProviderTokenReturnsControlledAuthError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accessToken": "invalid-token"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_OAUTH_TOKEN"));
    }

    @Test
    void openAiGeneratorReturnsGeneratedMessageWhenProviderSucceeds() throws Exception {
        openAiMode = OpenAiMode.SUCCESS;
        TokenPair tokenPair = devLogin("openai-success-user");

        try {
            mockMvc.perform(post("/api/v1/messages/generate")
                            .header("Authorization", bearer(tokenPair.accessToken()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(messageBody()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.flowerId").value(1))
                    .andExpect(jsonPath("$.data.message").value("OpenAI가 작성한 따뜻한 선물 메시지입니다."));
        } finally {
            openAiMode = OpenAiMode.ERROR;
        }
    }

    @Test
    void openAiGeneratorFallsBackToTemplateWhenProviderFails() throws Exception {
        openAiMode = OpenAiMode.ERROR;
        TokenPair tokenPair = devLogin("openai-fallback-user");

        mockMvc.perform(post("/api/v1/messages/generate")
                        .header("Authorization", bearer(tokenPair.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowerId").value(1))
                .andExpect(jsonPath("$.data.message", containsString("지은님께")))
                .andExpect(jsonPath("$.data.message", containsString("장미")));
    }

    private TokenPair devLogin(String oauthId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login/dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oauthId": "%s",
                                  "email": "%s@example.com",
                                  "nickname": "민화유저",
                                  "role": "ROLE_USER"
                                }
                                """.formatted(oauthId, oauthId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return new TokenPair(
                JsonPath.read(response, "$.data.accessToken"),
                JsonPath.read(response, "$.data.refreshToken")
        );
    }

    private TokenPair socialLogin(String provider, String accessToken) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login/{provider}", provider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accessToken": "%s"
                                }
                                """.formatted(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return new TokenPair(
                JsonPath.read(response, "$.data.accessToken"),
                JsonPath.read(response, "$.data.refreshToken")
        );
    }

    private String messageBody() {
        return """
                {
                  "flowerId": 1,
                  "selectedTagIds": [5, 9],
                  "senderName": "민수",
                  "receiverName": "지은"
                }
                """;
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private static void startServer() {
        if (server != null) {
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/kakao/userinfo", exchange -> {
                if (!"Bearer valid-kakao-token".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                    respond(exchange, 401, "{}");
                    return;
                }
                respond(exchange, 200, """
                        {
                          "id": 123456789,
                          "kakao_account": {
                            "email": "kakao-user@example.com",
                            "profile": {
                              "nickname": "카카오유저"
                            }
                          }
                        }
                        """);
            });
            server.createContext("/naver/userinfo", exchange -> {
                if (!"Bearer valid-naver-token".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                    respond(exchange, 401, "{}");
                    return;
                }
                respond(exchange, 200, """
                        {
                          "response": {
                            "id": "naver-user-1",
                            "email": "naver-user@example.com",
                            "nickname": "네이버유저"
                          }
                        }
                        """);
            });
            server.createContext("/v1/responses", exchange -> {
                if (openAiMode == OpenAiMode.SUCCESS) {
                    respond(exchange, 200, """
                            {
                              "output_text": "OpenAI가 작성한 따뜻한 선물 메시지입니다."
                            }
                            """);
                    return;
                }
                respond(exchange, 500, "{}");
            });
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start fake integration server.", exception);
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private enum OpenAiMode {
        SUCCESS,
        ERROR
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }
}
