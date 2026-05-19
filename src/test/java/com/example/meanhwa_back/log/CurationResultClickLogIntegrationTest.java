package com.example.meanhwa_back.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CurationResultClickLogIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActionLogRepository actionLogRepository;

    @Test
    void clickLogStoresWizardSelectionsSnapshot() throws Exception {
        long before = actionLogRepository.countByActionType(ActionType.CURATION_RESULT_CLICK);

        mockMvc.perform(post("/api/v1/action-logs/curation-result-click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "flowerId": 1,
                                  "source": "curation-v2",
                                  "flowVersion": "2026-05-v1",
                                  "selections": [
                                    { "step": "OCCASION", "code": "BIRTHDAY" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        ActionLog log = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.CURATION_RESULT_CLICK).get(0);
        assertThat(actionLogRepository.countByActionType(ActionType.CURATION_RESULT_CLICK)).isEqualTo(before + 1);
        assertThat(log.getActionData())
                .contains("\"source\":\"curation-v2\"")
                .contains("\"flowVersion\":\"2026-05-v1\"")
                .contains("\"code\":\"BIRTHDAY\"");
    }
}
