package com.example.meanhwa_back;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.log.domain.ActionLog;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.repository.ActionLogRepository;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class MeanhwaBackApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlowerRepository flowerRepository;

    @Autowired
    private ActionLogRepository actionLogRepository;

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
    void keywordSearchRecordsDictionarySearchActionLog() throws Exception {
        long beforeCount = actionLogRepository.countByActionType(ActionType.DICTIONARY_SEARCH);

        mockMvc.perform(get("/api/v1/flowers")
                        .param("keyword", "사랑")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        List<ActionLog> logs = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.DICTIONARY_SEARCH);
        org.assertj.core.api.Assertions.assertThat(logs).hasSize((int) beforeCount + 1);
        org.assertj.core.api.Assertions.assertThat(logs.get(0).getUserId()).isNull();
        org.assertj.core.api.Assertions.assertThat(logs.get(0).getActionData()).contains("\"keyword\":\"사랑\"");
    }

    @Test
    void flowerListWithoutKeywordDoesNotRecordDictionarySearchActionLog() throws Exception {
        long beforeCount = actionLogRepository.countByActionType(ActionType.DICTIONARY_SEARCH);

        mockMvc.perform(get("/api/v1/flowers"))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(actionLogRepository.countByActionType(ActionType.DICTIONARY_SEARCH))
                .isEqualTo(beforeCount);
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
    void flowerDetailRecordsActionLog() throws Exception {
        long beforeCount = actionLogRepository.countByActionType(ActionType.FLOWER_DETAIL_VIEW);

        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 1))
                .andExpect(status().isOk());

        List<ActionLog> logs = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.FLOWER_DETAIL_VIEW);
        org.assertj.core.api.Assertions.assertThat(logs).hasSize((int) beforeCount + 1);
        org.assertj.core.api.Assertions.assertThat(logs.get(0).getActionData()).contains("\"flowerId\":1");
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
    void curationRecordsActionLog() throws Exception {
        long beforeCount = actionLogRepository.countByActionType(ActionType.CURATION_START);

        mockMvc.perform(get("/api/v1/curation")
                        .param("tagIds", "5", "9")
                        .param("isPetSafe", "true")
                        .param("priceRange", "MEDIUM"))
                .andExpect(status().isOk());

        List<ActionLog> logs = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.CURATION_START);
        org.assertj.core.api.Assertions.assertThat(logs).hasSize((int) beforeCount + 1);
        org.assertj.core.api.Assertions.assertThat(logs.get(0).getUserId()).isNull();
        org.assertj.core.api.Assertions.assertThat(logs.get(0).getActionData())
                .contains("\"tagIds\":[5,9]")
                .contains("\"priceRange\":\"MEDIUM\"")
                .contains("\"resultFlowerIds\":[1]");
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

    @Test
    void devLoginReturnsAccessAndRefreshTokens() throws Exception {
        String body = devLoginBody("auth-user-1", "ROLE_USER");

        mockMvc.perform(post("/api/v1/auth/login/dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresInSeconds").value(1800))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void unsupportedOAuthProviderReturnsError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devLoginBody("kakao-user-1", "ROLE_USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_OAUTH_PROVIDER"));
    }

    @Test
    void refreshRotatesRefreshTokenAndRejectsOldToken() throws Exception {
        TokenPair tokenPair = login("refresh-user-1", "ROLE_USER");

        String refreshBody = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(tokenPair.refreshToken());
        TokenPair rotatedTokenPair = extractTokenPair(mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(rotatedTokenPair.refreshToken())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(rotatedTokenPair.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
    }

    @Test
    void usersMeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/users/me/likes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void userCanReadMeWithBearerToken() throws Exception {
        TokenPair tokenPair = login("me-user-1", "ROLE_USER");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.oauthId").value("me-user-1"))
                .andExpect(jsonPath("$.data.nickname").value("민화유저"))
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
    }

    @Test
    void authenticatedCurationActionLogStoresUserId() throws Exception {
        TokenPair tokenPair = login("log-user-1", "ROLE_USER");
        Long userId = extractUserId(tokenPair.accessToken());

        mockMvc.perform(get("/api/v1/curation")
                        .header("Authorization", bearer(tokenPair.accessToken()))
                        .param("tagIds", "5", "9"))
                .andExpect(status().isOk());

        List<ActionLog> logs = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.CURATION_START);
        org.assertj.core.api.Assertions.assertThat(logs.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void userRoleCannotAccessAdminApi() throws Exception {
        TokenPair tokenPair = login("admin-denied-user-1", "ROLE_USER");

        mockMvc.perform(get("/api/v1/admin/flowers")
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void adminFlowerCmsCreatesUpdatesMapsAndSoftDeletesFlower() throws Exception {
        TokenPair admin = login("admin-flower-user-1", "ROLE_ADMIN");

        String createdResponse = mockMvc.perform(post("/api/v1/admin/flowers")
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminFlowerBody("관리자테스트꽃", "처음 의미", "LOW")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("관리자테스트꽃"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        Integer flowerId = JsonPath.read(createdResponse, "$.data.id");

        mockMvc.perform(put("/api/v1/admin/flowers/{flowerId}", flowerId)
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminFlowerBody("관리자테스트꽃수정", "수정 의미", "MEDIUM")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("관리자테스트꽃수정"))
                .andExpect(jsonPath("$.data.priceRange").value("MEDIUM"));

        mockMvc.perform(put("/api/v1/admin/flowers/{flowerId}/tags", flowerId)
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tags": [
                                    {"tagId": 1, "weight": 5},
                                    {"tagId": 10, "weight": 3}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags", hasSize(2)));

        mockMvc.perform(get("/api/v1/flowers/{flowerId}", flowerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("관리자테스트꽃수정"));

        mockMvc.perform(delete("/api/v1/admin/flowers/{flowerId}", flowerId)
                        .header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/flowers/{flowerId}", flowerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("FLOWER_NOT_FOUND"));
    }

    @Test
    void adminTagCmsRejectsDuplicateActiveTagAndSoftDeletesTag() throws Exception {
        TokenPair admin = login("admin-tag-user-1", "ROLE_ADMIN");

        mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "EVENT",
                                  "name": "생일"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_TAG"));

        String createdResponse = mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "EVENT",
                                  "name": "관리자태그테스트"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("관리자태그테스트"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        Integer tagId = JsonPath.read(createdResponse, "$.data.id");

        mockMvc.perform(put("/api/v1/admin/tags/{tagId}", tagId)
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "EMOTION",
                                  "name": "관리자태그수정"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value("EMOTION"))
                .andExpect(jsonPath("$.data.name").value("관리자태그수정"));

        mockMvc.perform(delete("/api/v1/admin/tags/{tagId}", tagId)
                        .header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/curation")
                        .param("tagIds", tagId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TAG_NOT_FOUND"));
    }

    @Test
    void adminMappingRejectsDuplicateTagIds() throws Exception {
        TokenPair admin = login("admin-mapping-user-1", "ROLE_ADMIN");

        mockMvc.perform(put("/api/v1/admin/flowers/{flowerId}/tags", 1)
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tags": [
                                    {"tagId": 1, "weight": 5},
                                    {"tagId": 1, "weight": 3}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MAPPING"));
    }

    @Test
    void adminImageUploadUsesFakeStorageInTestProfile() throws Exception {
        TokenPair admin = login("admin-upload-user-1", "ROLE_ADMIN");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "flower.png",
                "image/png",
                "fake-image".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/admin/uploads/images")
                        .file(file)
                        .header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrl", containsString("https://fake.meanhwa.local/uploads/flowers/")))
                .andExpect(jsonPath("$.data.originalFilename").value("flower.png"))
                .andExpect(jsonPath("$.data.contentType").value("image/png"))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    void adminImageUploadRejectsInvalidContentType() throws Exception {
        TokenPair admin = login("admin-upload-user-2", "ROLE_ADMIN");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "flower.txt",
                "text/plain",
                "not-image".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/admin/uploads/images")
                        .file(file)
                        .header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_TYPE"));
    }

    @Test
    void userLikesAreIdempotentAndScopedByUser() throws Exception {
        TokenPair firstUser = login("like-user-1", "ROLE_USER");
        TokenPair secondUser = login("like-user-2", "ROLE_USER");

        mockMvc.perform(post("/api/v1/users/me/likes/{flowerId}", 1)
                        .header("Authorization", bearer(firstUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
        mockMvc.perform(post("/api/v1/users/me/likes/{flowerId}", 1)
                        .header("Authorization", bearer(firstUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
        mockMvc.perform(post("/api/v1/users/me/likes/{flowerId}", 2)
                        .header("Authorization", bearer(secondUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2));

        mockMvc.perform(get("/api/v1/users/me/likes")
                        .header("Authorization", bearer(firstUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1));
        mockMvc.perform(get("/api/v1/users/me/likes")
                        .header("Authorization", bearer(secondUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(2));

        mockMvc.perform(delete("/api/v1/users/me/likes/{flowerId}", 1)
                        .header("Authorization", bearer(firstUser.accessToken())))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/users/me/likes/{flowerId}", 1)
                        .header("Authorization", bearer(firstUser.accessToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/me/likes")
                        .header("Authorization", bearer(firstUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void flowerDetailStoresHistoryOnlyForAuthenticatedUser() throws Exception {
        TokenPair tokenPair = login("history-user-1", "ROLE_USER");

        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/users/me/histories")
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));

        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 1)
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/flowers/{flowerId}", 1)
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/me/histories")
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void historiesKeepLatestFiftyFlowers() throws Exception {
        TokenPair tokenPair = login("history-user-2", "ROLE_USER");
        List<Flower> flowers = new ArrayList<>();
        for (int index = 0; index < 51; index++) {
            flowers.add(flowerRepository.save(new Flower(
                    "테스트식물" + index,
                    "https://cdn.meanhwa.example/test/" + index + ".jpg",
                    "테스트 의미 " + index,
                    ManagementLevel.EASY,
                    "테스트 관리법",
                    false,
                    PriceRange.LOW
            )));
        }

        for (Flower flower : flowers) {
            mockMvc.perform(get("/api/v1/flowers/{flowerId}", flower.getId())
                            .header("Authorization", bearer(tokenPair.accessToken())))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/users/me/histories")
                        .header("Authorization", bearer(tokenPair.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(50)))
                .andExpect(jsonPath("$.data[*].id", not(hasItem(flowers.get(0).getId().intValue()))));
    }

    @Test
    void curationResultClickRecordsActionLogForAnonymousAndAuthenticatedUsers() throws Exception {
        long beforeCount = actionLogRepository.countByActionType(ActionType.CURATION_RESULT_CLICK);
        String body = """
                {
                  "flowerId": 1,
                  "tagIds": [5, 9],
                  "rank": 1,
                  "score": 10,
                  "source": "curation"
                }
                """;

        mockMvc.perform(post("/api/v1/action-logs/curation-result-click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        List<ActionLog> anonymousLogs = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.CURATION_RESULT_CLICK);
        org.assertj.core.api.Assertions.assertThat(anonymousLogs).hasSize((int) beforeCount + 1);
        org.assertj.core.api.Assertions.assertThat(anonymousLogs.get(0).getUserId()).isNull();
        org.assertj.core.api.Assertions.assertThat(anonymousLogs.get(0).getActionData())
                .contains("\"flowerId\":1")
                .contains("\"rank\":1");

        TokenPair tokenPair = login("click-log-user-1", "ROLE_USER");
        Long userId = extractUserId(tokenPair.accessToken());
        mockMvc.perform(post("/api/v1/action-logs/curation-result-click")
                        .header("Authorization", bearer(tokenPair.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        List<ActionLog> authenticatedLogs = actionLogRepository.findByActionTypeOrderByIdDesc(ActionType.CURATION_RESULT_CLICK);
        org.assertj.core.api.Assertions.assertThat(authenticatedLogs).hasSize((int) beforeCount + 2);
        org.assertj.core.api.Assertions.assertThat(authenticatedLogs.get(0).getUserId()).isEqualTo(userId);
    }

    private TokenPair login(String oauthId, String role) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login/dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devLoginBody(oauthId, role)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return extractTokenPair(response);
    }

    private TokenPair extractTokenPair(String response) {
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

    private String adminFlowerBody(String name, String coreMeaning, String priceRange) {
        return """
                {
                  "name": "%s",
                  "imageUrl": "https://cdn.meanhwa.example/admin-test.jpg",
                  "coreMeaning": "%s",
                  "managementLevel": "EASY",
                  "managementInfo": "관리자 테스트 관리법",
                  "isToxicToPets": false,
                  "priceRange": "%s"
                }
                """.formatted(name, coreMeaning, priceRange);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private Long extractUserId(String accessToken) {
        String[] parts = accessToken.split("\\.");
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return Long.valueOf(JsonPath.read(payload, "$.sub"));
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }
}
