package com.example.meanhwa_back.curation.wizard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

/**
 * 분기형 큐레이션 위저드 P1(flow/options) · P2(results) 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CurationWizardIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActionLogRepository actionLogRepository;

    @Test
    void getFlowReturnsSixSteps() throws Exception {
        mockMvc.perform(get("/api/v1/curation/flow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowVersion").value("2026-05-v1"))
                .andExpect(jsonPath("$.data.totalSteps").value(6))
                .andExpect(jsonPath("$.data.steps", hasSize(6)))
                .andExpect(jsonPath("$.data.steps[2].defaultQuestionTitle").value("어떤 마음을 전하고 싶나요?"))
                .andExpect(jsonPath("$.data.steps[3].defaultQuestionSubtitle")
                        .value("전하고 싶은 마음을 조금 더 구체적으로 들려주세요."));
    }

    @Test
    void getRecipientOptionsBranchesByOccasion() throws Exception {
        String selections = """
                [{"step":"OCCASION","code":"PROMOTION"}]
                """;
        mockMvc.perform(get("/api/v1/curation/steps/RECIPIENT/options")
                        .param("selections", selections))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.step").value("RECIPIENT"))
                .andExpect(jsonPath("$.data.options[0].code").value("COLLEAGUE_JUNIOR"));
    }

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
                .andExpect(jsonPath("$.data.content[0].score").isNumber());
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
    void stepOptionsRejectInvalidPriorBranchCombination() throws Exception {
        mockMvc.perform(get("/api/v1/curation/steps/FLOWER_MEANING/options")
                        .param("selections", """
                                [
                                  { "step": "OCCASION", "code": "PROMOTION" },
                                  { "step": "RECIPIENT", "code": "COLLEAGUE_JUNIOR" },
                                  { "step": "EMOTION", "code": "LOVE" }
                                ]
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURATION_SELECTION"));
    }

    @Test
    void stepOptionsRejectCurrentOrFutureSelections() throws Exception {
        mockMvc.perform(get("/api/v1/curation/steps/EMOTION/options")
                        .param("selections", """
                                [
                                  { "step": "OCCASION", "code": "BIRTHDAY" },
                                  { "step": "EMOTION", "code": "LOVE" }
                                ]
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURATION_SELECTION"));
    }

    @Test
    void getFlowerMeaningOptionsReturnsLabelsWhenPriorSelectionsMatchBranches() throws Exception {
        String selections = """
                [
                  {"step":"OCCASION","code":"PROMOTION"},
                  {"step":"RECIPIENT","code":"COLLEAGUE_JUNIOR"},
                  {"step":"EMOTION","code":"CELEBRATION"}
                ]
                """;
        mockMvc.perform(get("/api/v1/curation/steps/FLOWER_MEANING/options")
                        .param("selections", selections))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.step").value("FLOWER_MEANING"))
                .andExpect(jsonPath("$.data.options[0].code").value("CELEBRATION_1"))
                .andExpect(jsonPath("$.data.options[3].code").value("CELEBRATION_4"));
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
    void flowerMeaningOptionsUsePdfLabels() throws Exception {
        mockMvc.perform(get("/api/v1/curation/steps/FLOWER_MEANING/options")
                        .param("selections", """
                                [
                                  { "step": "OCCASION", "code": "BIRTHDAY" },
                                  { "step": "RECIPIENT", "code": "LOVER" },
                                  { "step": "EMOTION", "code": "LOVE" }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionTitle").value("연인에게 전달하고 싶은 꽃말은 무엇인가요?"))
                .andExpect(jsonPath("$.data.questionSubtitle")
                        .value("전하고 싶은 마음을 조금 더 구체적으로 들려주세요."))
                .andExpect(jsonPath("$.data.options[0].code").value("LOVE_1"))
                .andExpect(jsonPath("$.data.options[0].label").value("변함없는 마음"))
                .andExpect(jsonPath("$.data.options[3].code").value("LOVE_4"))
                .andExpect(jsonPath("$.data.options[3].label").value("진실한 사랑"));

        mockMvc.perform(get("/api/v1/curation/steps/FLOWER_MEANING/options")
                        .param("selections", """
                                [
                                  { "step": "OCCASION", "code": "BIRTHDAY" },
                                  { "step": "RECIPIENT", "code": "FRIEND" },
                                  { "step": "EMOTION", "code": "SUPPORT" }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.options[1].code").value("SUPPORT_2"))
                .andExpect(jsonPath("$.data.options[1].label").value("변치 않는 우정"))
                .andExpect(jsonPath("$.data.options[2].code").value("SUPPORT_3"))
                .andExpect(jsonPath("$.data.options[2].label").value("찬란한 미소"));

        mockMvc.perform(get("/api/v1/curation/steps/FLOWER_MEANING/options")
                        .param("selections", """
                                [
                                  { "step": "OCCASION", "code": "RECOVERY" },
                                  { "step": "RECIPIENT", "code": "FAMILY" },
                                  { "step": "EMOTION", "code": "COMFORT" }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionTitle").value("가족에게 전달하고 싶은 꽃말은 무엇인가요?"))
                .andExpect(jsonPath("$.data.options[3].code").value("COMFORT_4"))
                .andExpect(jsonPath("$.data.options[3].label").value("마음의 안계"));

        mockMvc.perform(get("/api/v1/curation/steps/FLOWER_MEANING/options")
                        .param("selections", """
                                [
                                  { "step": "OCCASION", "code": "RECOVERY" },
                                  { "step": "RECIPIENT", "code": "FAMILY" },
                                  { "step": "EMOTION", "code": "GET_WELL" }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.options[1].code").value("GET_WELL_2"))
                .andExpect(jsonPath("$.data.options[1].label").value("다시 찾은 활력"));
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
                .contains("\"step\":\"BUDGET\"");
    }
}
