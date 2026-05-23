package com.example.meanhwa_back.curation.wizard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.meanhwa_back.log.domain.ActionLog;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.repository.ActionLogRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** 분기형 큐레이션 위저드 결과 API 통합 테스트. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CurationWizardIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActionLogRepository actionLogRepository;

    @Test
    void postResultsReturnsScoredFlowers() throws Exception {
        String body = """
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
        mockMvc.perform(post("/api/v1/curation/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.content[0].score").isNumber())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.totalPages").isNumber())
                .andExpect(jsonPath("$.data.hasNext").isBoolean())
                .andExpect(jsonPath("$.data.curationResultId").value(nullValue()));
    }

    @Test
    void postResultsAcceptsSelectionsRegardlessOfArrayOrder() throws Exception {
        String body = """
                {
                  "flowVersion": "2026-05-v1",
                  "selections": [
                    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
                    { "step": "BUDGET", "code": "BUDGET_MEDIUM" },
                    { "step": "SPACE", "code": "DESK_SMALL" },
                    { "step": "EMOTION", "code": "LOVE" },
                    { "step": "RECIPIENT", "code": "LOVER" },
                    { "step": "OCCASION", "code": "BIRTHDAY" }
                  ],
                  "page": 0,
                  "size": 5
                }
                """;
        mockMvc.perform(post("/api/v1/curation/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))));
    }

    @Test
    void postResultsRejectsInvalidBranchCombinationRegardlessOfArrayOrder() throws Exception {
        String body = """
                {
                  "flowVersion": "2026-05-v1",
                  "selections": [
                    { "step": "BUDGET", "code": "BUDGET_MEDIUM" },
                    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
                    { "step": "OCCASION", "code": "PROMOTION" },
                    { "step": "SPACE", "code": "DESK_SMALL" },
                    { "step": "RECIPIENT", "code": "COLLEAGUE_JUNIOR" },
                    { "step": "EMOTION", "code": "LOVE" }
                  ],
                  "page": 0,
                  "size": 5
                }
                """;
        mockMvc.perform(post("/api/v1/curation/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURATION_SELECTION"));
    }

    @Test
    void duplicateStepReturnsInvalidCurationSelection() throws Exception {
        String body = """
                {
                  "flowVersion": "2026-05-v1",
                  "selections": [
                    { "step": "OCCASION", "code": "BIRTHDAY" },
                    { "step": "OCCASION", "code": "GRADUATION" },
                    { "step": "RECIPIENT", "code": "LOVER" },
                    { "step": "EMOTION", "code": "LOVE" },
                    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
                    { "step": "SPACE", "code": "DESK_SMALL" }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/curation/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURATION_SELECTION"));
    }

    @Test
    void missingStepReturnsIncompleteCurationSelection() throws Exception {
        String body = """
                {
                  "flowVersion": "2026-05-v1",
                  "selections": [
                    { "step": "OCCASION", "code": "BIRTHDAY" },
                    { "step": "RECIPIENT", "code": "LOVER" },
                    { "step": "EMOTION", "code": "LOVE" },
                    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
                    { "step": "SPACE", "code": "DESK_SMALL" }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/curation/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCOMPLETE_CURATION_SELECTION"));
    }

    @Test
    void postResultsRecordsCurationStartActionLogWithV2Source() throws Exception {
        long beforeCount = actionLogRepository.countByActionType(ActionType.CURATION_START);

        String body = """
                {
                  "flowVersion": "2026-05-v1",
                  "selections": [
                    { "step": "OCCASION", "code": "BIRTHDAY" },
                    { "step": "RECIPIENT", "code": "LOVER" },
                    { "step": "EMOTION", "code": "LOVE" },
                    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
                    { "step": "SPACE", "code": "DESK_SMALL" },
                    { "step": "BUDGET", "code": "BUDGET_MEDIUM" }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/curation/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        ActionLog latest = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.CURATION_START).get(0);
        assertThat(actionLogRepository.countByActionType(ActionType.CURATION_START)).isEqualTo(beforeCount + 1);
        assertThat(latest.getActionData())
                .contains("\"source\":\"curation-v2\"")
                .contains("\"flowVersion\":\"2026-05-v1\"")
                .contains("\"step\":\"BUDGET\"")
                .contains("\"totalElements\":")
                .contains("\"resultFlowerIds\":[");
    }
}
