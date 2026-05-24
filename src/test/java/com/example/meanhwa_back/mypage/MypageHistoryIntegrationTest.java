package com.example.meanhwa_back.mypage;

import java.nio.charset.StandardCharsets;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MypageHistoryIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void authenticatedCurationResultIsSavedAndCanBeReplayed() throws Exception {
        String token = login("curation-history-user");

        String curationResponse = mockMvc.perform(post("/api/v1/curation/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(curationBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.curationResultId").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        Integer resultId = JsonPath.read(curationResponse, "$.data.curationResultId");

        mockMvc.perform(get("/api/v1/users/me/curation-results/latest")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(resultId))
                .andExpect(jsonPath("$.data.flowVersion").value("2026-05-v1"))
                .andExpect(jsonPath("$.data.selections[0].label").value("생일"))
                .andExpect(jsonPath("$.data.recommendations[0].rank").value(1));

        mockMvc.perform(get("/api/v1/users/me/curation-results"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users/me/curation-results/{resultId}", resultId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(resultId))
                .andExpect(jsonPath("$.data.recommendations[0].flowerId").isNumber());
    }

    @Test
    void generatedMessagesAreSavedAndListedForCurrentUser() throws Exception {
        String token = login("message-history-user");

        String curationResponse = mockMvc.perform(post("/api/v1/curation/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(curationBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curationResultId").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        Integer resultId = JsonPath.read(curationResponse, "$.data.curationResultId");

        mockMvc.perform(post("/api/v1/messages/generate")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageBody(resultId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.flowerId").value(1))
                .andExpect(jsonPath("$.data.flowerName").value("장미"))
                .andExpect(jsonPath("$.data.curationResultId").value(resultId))
                .andExpect(jsonPath("$.data.message").isNotEmpty());

        mockMvc.perform(get("/api/v1/users/me/messages")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].flowerId").value(1))
                .andExpect(jsonPath("$.data.content[0].curationResultId").value(resultId))
                .andExpect(jsonPath("$.data.content[0].senderName").value("민수"));
    }

    @Test
    void anonymousCurationResultDoesNotReturnSavedResultId() throws Exception {
        mockMvc.perform(post("/api/v1/curation/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(curationBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.curationResultId").doesNotExist());
    }

    private String login(String oauthId) throws Exception {
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
        return JsonPath.read(response, "$.data.accessToken");
    }

    private String curationBody() {
        return """
                {
                  "flowVersion": "2026-05-v1",
                  "selections": [
                    { "step": "OCCASION", "code": "BIRTHDAY" },
                    { "step": "RECIPIENT", "code": "LOVER" },
                    { "step": "EMOTION", "code": "LOVE" },
                    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
                    { "step": "SPACE", "code": "DESK_SMALL" },
                    { "step": "BUDGET", "code": "BUDGET_MEDIUM" }
                  ],
                  "page": 0,
                  "size": 5
                }
                """;
    }

    private String messageBody(Integer curationResultId) {
        return """
                {
                  "flowerId": 1,
                  "selectedTagIds": [5, 9],
                  "curationResultId": %d,
                  "senderName": "민수",
                  "receiverName": "지은"
                }
                """.formatted(curationResultId);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
