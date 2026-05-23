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
}
