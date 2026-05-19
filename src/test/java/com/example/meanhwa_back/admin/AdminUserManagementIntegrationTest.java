package com.example.meanhwa_back.admin;

import java.nio.charset.StandardCharsets;

import com.example.meanhwa_back.log.domain.ActionLog;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.repository.ActionLogRepository;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 관리자 회원 관리 API 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserManagementIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActionLogRepository actionLogRepository;

    @Test
    void nonAdminCannotListUsers() throws Exception {
        String token = login("admin-user-deny-list", Role.ROLE_USER.name());
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListsUsersAndUpdatesRole() throws Exception {
        String targetOauthId = "admin-target-user-1";
        String adminOauthId = "admin-operator-1";
        String adminToken = login(adminOauthId, Role.ROLE_ADMIN.name());
        login(targetOauthId, Role.ROLE_USER.name());

        Long adminUserId = userRepository.findByProviderAndOauthId(
                com.example.meanhwa_back.auth.domain.OAuthProvider.DEV,
                adminOauthId
        ).orElseThrow().getId();
        Long targetUserId = userRepository.findByProviderAndOauthId(
                com.example.meanhwa_back.auth.domain.OAuthProvider.DEV,
                targetOauthId
        ).orElseThrow().getId();

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", targetOauthId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].oauthId").value(targetOauthId))
                .andExpect(jsonPath("$.data.content[0].role").value("ROLE_USER"));

        mockMvc.perform(get("/api/v1/admin/users/{userId}", targetUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(targetUserId))
                .andExpect(jsonPath("$.data.likeCount").isNumber());

        long beforeRoleChangeLogCount = actionLogRepository.countByActionType(ActionType.ADMIN_USER_ROLE_CHANGE);
        mockMvc.perform(put("/api/v1/admin/users/{userId}/role", targetUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "role": "ROLE_ADMIN" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));

        assertThat(
                userRepository.findById(targetUserId).orElseThrow().getRole()
        ).isEqualTo(Role.ROLE_ADMIN);
        ActionLog latestRoleChangeLog = actionLogRepository
                .findByActionTypeOrderByIdDesc(ActionType.ADMIN_USER_ROLE_CHANGE)
                .get(0);
        assertThat(actionLogRepository.countByActionType(ActionType.ADMIN_USER_ROLE_CHANGE))
                .isEqualTo(beforeRoleChangeLogCount + 1);
        assertThat(latestRoleChangeLog.getUserId()).isEqualTo(adminUserId);
        assertThat(latestRoleChangeLog.getActionData())
                .contains("\"actorUserId\":" + adminUserId)
                .contains("\"targetUserId\":" + targetUserId)
                .contains("\"previousRole\":\"ROLE_USER\"")
                .contains("\"newRole\":\"ROLE_ADMIN\"");
    }

    @Test
    void adminCannotChangeOwnRole() throws Exception {
        String adminToken = login("admin-self-role-1", Role.ROLE_ADMIN.name());
        Long adminId = userRepository.findByProviderAndOauthId(
                com.example.meanhwa_back.auth.domain.OAuthProvider.DEV,
                "admin-self-role-1"
        ).orElseThrow().getId();

        long beforeRoleChangeLogCount = actionLogRepository.countByActionType(ActionType.ADMIN_USER_ROLE_CHANGE);
        mockMvc.perform(put("/api/v1/admin/users/{userId}/role", adminId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "role": "ROLE_USER" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CANNOT_CHANGE_OWN_ROLE"));
        assertThat(actionLogRepository.countByActionType(ActionType.ADMIN_USER_ROLE_CHANGE))
                .isEqualTo(beforeRoleChangeLogCount);
    }

    private String login(String oauthId, String role) throws Exception {
        String body = """
                {
                  "oauthId": "%s",
                  "email": "%s@example.com",
                  "nickname": "테스트",
                  "role": "%s"
                }
                """.formatted(oauthId, oauthId, role);
        String response = mockMvc.perform(post("/api/v1/auth/login/dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(response, "$.data.accessToken");
    }
}
