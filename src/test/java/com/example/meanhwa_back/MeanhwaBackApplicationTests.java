package com.example.meanhwa_back;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class MeanhwaBackApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void getFlowersReturnsPagedFlowers() throws Exception {
        mockMvc.perform(get("/api/v1/flowers")
                        .param("keyword", "사랑")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5));
    }

    @Test
    void getFlowerReturnsDetailWithTags() throws Exception {
        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("장미"))
                .andExpect(jsonPath("$.data.isPetSafe").value(true))
                .andExpect(jsonPath("$.data.tags", hasSize(greaterThan(0))));
    }

    @Test
    void getFlowerReturnsNotFoundError() throws Exception {
        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("FLOWER_NOT_FOUND"));
    }

    @Test
    void getTagsReturnsGroupedTags() throws Exception {
        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].category").value("EVENT"))
                .andExpect(jsonPath("$.data[0].tags", hasSize(greaterThan(0))));
    }

    @Test
    void curateReturnsScoreSortedAndFilteredFlowers() throws Exception {
        mockMvc.perform(get("/api/v1/curation")
                        .param("tagIds", "5", "9")
                        .param("isPetSafe", "true")
                        .param("priceRange", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].flowerId").value(1))
                .andExpect(jsonPath("$.data.content[0].score").value(10))
                .andExpect(jsonPath("$.data.content[0].isPetSafe").value(true))
                .andExpect(jsonPath("$.data.content[0].priceRange").value("MEDIUM"))
                .andExpect(jsonPath("$.data.content[0].matchedTags", hasSize(2)));
    }

    @Test
    void curateReturnsInvalidPriceRangeError() throws Exception {
        mockMvc.perform(get("/api/v1/curation")
                        .param("priceRange", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_PRICE_RANGE"));
    }

    @Test
    void generateMessageReturnsTemplateMessage() throws Exception {
        String body = """
                {
                  "flowerId": 1,
                  "selectedTagIds": [5, 9],
                  "senderName": "민수",
                  "receiverName": "지은"
                }
                """;

        mockMvc.perform(post("/api/v1/messages/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.flowerId").value(1))
                .andExpect(jsonPath("$.data.message", containsString("지은님께")))
                .andExpect(jsonPath("$.data.message", containsString("장미")))
                .andExpect(jsonPath("$.data.message", containsString("사랑과 열정")));
    }

    @Test
    void generateMessageReturnsFlowerNotFoundError() throws Exception {
        String body = """
                {
                  "flowerId": 9999,
                  "selectedTagIds": [5, 9],
                  "senderName": "민수",
                  "receiverName": "지은"
                }
                """;

        mockMvc.perform(post("/api/v1/messages/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("FLOWER_NOT_FOUND"));
    }
}
