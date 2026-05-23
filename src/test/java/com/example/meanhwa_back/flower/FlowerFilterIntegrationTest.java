package com.example.meanhwa_back.flower;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * 꽃 사전 필터링과 상세 응답 계약을 검증하는 통합 테스트.
 * 검색 조건 조합이 페이징 전에 적용되고 잘못된 필터가 통제된 오류로 반환되는지 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlowerFilterIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void flowerListAppliesDictionaryFiltersBeforePaging() throws Exception {
        mockMvc.perform(get("/api/v1/flowers")
                        .param("keyword", "사랑")
                        .param("priceRange", "MEDIUM")
                        .param("isPetSafe", "true")
                        .param("managementLevel", "NORMAL")
                        .param("tagIds", "5")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.content[0].priceRange").value("MEDIUM"))
                .andExpect(jsonPath("$.data.content[0].isPetSafe").value(true))
                .andExpect(jsonPath("$.data.content[0].managementLevel").value("NORMAL"))
                .andExpect(jsonPath("$.data.content[0].description").exists());
    }

    @Test
    void invalidFlowerFilterReturnsControlledError() throws Exception {
        mockMvc.perform(get("/api/v1/flowers")
                        .param("priceRange", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FLOWER_FILTER"));
    }

    @Test
    void flowerDetailIncludesDescriptionOnlyForFlowerDetailText() throws Exception {
        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.scientificName").doesNotExist())
                .andExpect(jsonPath("$.data.origin").doesNotExist())
                .andExpect(jsonPath("$.data.bloomingSeason").doesNotExist())
                .andExpect(jsonPath("$.data.scent").doesNotExist())
                .andExpect(jsonPath("$.data.managementInfo").doesNotExist());
    }

    @Test
    void nonPositiveFlowerIdsAndTagIdsReturnInvalidRequest() throws Exception {
        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));

        mockMvc.perform(get("/api/v1/flowers")
                        .param("tagIds", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }
}
