package com.example.meanhwa_back.admin.service;

import java.util.Map;

import com.example.meanhwa_back.admin.dto.AdminUserDetailResponse;
import com.example.meanhwa_back.admin.dto.AdminUserRoleUpdateRequest;
import com.example.meanhwa_back.admin.dto.AdminUserSummaryResponse;
import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.common.web.PageRequestUtils;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.service.ActionLogService;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;
import com.example.meanhwa_back.user.repository.UserHistoryRepository;
import com.example.meanhwa_back.user.repository.UserLikeRepository;
import com.example.meanhwa_back.user.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 백오피스 회원 조회·권한 변경 서비스.
 * <p>소셜 로그인으로 생성된 {@link User} row를 대상으로 하며, 회원 생성·삭제는 제공하지 않는다.
 * (가입은 {@code POST /api/v1/auth/login/{provider}} 흐름만 사용)
 */
@Service
@Transactional(readOnly = true)
public class AdminUserService {
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ActionLogService actionLogService;

    public AdminUserService(
            UserRepository userRepository,
            UserLikeRepository userLikeRepository,
            UserHistoryRepository userHistoryRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            ActionLogService actionLogService
    ) {
        this.userRepository = userRepository;
        this.userLikeRepository = userLikeRepository;
        this.userHistoryRepository = userHistoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.actionLogService = actionLogService;
    }

    /**
     * 회원 목록 페이지 조회 (가입 최신순).
     *
     * @param keyword  email·nickname·oauthId 부분 검색 (null/blank면 전체)
     * @param provider 소셜 제공자 필터 (null이면 전체)
     * @param role     권한 필터 (null이면 전체)
     */
    public PageResponse<AdminUserSummaryResponse> listUsers(
            String keyword,
            String providerValue,
            String roleValue,
            int page,
            int size
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Role role = parseRoleFilter(roleValue);
        OAuthProvider provider = parseProviderFilter(providerValue);

        Page<User> result = userRepository.searchForAdmin(
                normalizedKeyword,
                role,
                provider,
                PageRequestUtils.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PageResponse.from(result.map(AdminUserSummaryResponse::from));
    }

    /** 단일 회원 상세 + 찜·최근 본 건수. */
    public AdminUserDetailResponse getUser(Long userId) {
        User user = getUserOrThrow(userId);
        long likeCount = userLikeRepository.countByUserId(userId);
        long historyCount = userHistoryRepository.countByUserId(userId);
        return AdminUserDetailResponse.from(user, likeCount, historyCount);
    }

    /**
     * 회원 {@link Role} 변경 (승격·회수).
     * <p>안전 규칙:
     * <ul>
     *   <li>요청을 보낸 관리자 본인의 role은 변경 불가 → DB 직접 수정 또는 다른 관리자에게 요청</li>
     *   <li>시스템에 {@code ROLE_ADMIN}이 1명뿐일 때 그 계정을 {@code ROLE_USER}로 내리지 못함</li>
     * </ul>
     * 권한 변경 후에는 대상 사용자가 <strong>재로그인</strong>해야 JWT에 반영된 role을 받는다.
     */
    @Transactional
    public AdminUserDetailResponse updateRole(Long userId, AdminUserRoleUpdateRequest request) {
        User target = getUserOrThrow(userId);
        Role newRole = request.role();
        User currentAdmin = authenticatedUserProvider.getCurrentUser();

        if (target.getId().equals(currentAdmin.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }
        if (target.getRole() == Role.ROLE_ADMIN && newRole == Role.ROLE_USER) {
            long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new BusinessException(ErrorCode.LAST_ADMIN_CANNOT_BE_DEMOTED);
            }
        }

        Role previousRole = target.getRole();
        target.updateRole(newRole);
        recordRoleChangeIfNeeded(currentAdmin, target, previousRole, newRole);
        return getUser(userId);
    }

    private void recordRoleChangeIfNeeded(User currentAdmin, User target, Role previousRole, Role newRole) {
        if (previousRole == newRole) {
            return;
        }
        actionLogService.record(ActionType.ADMIN_USER_ROLE_CHANGE, Map.of(
                "actorUserId", currentAdmin.getId(),
                "targetUserId", target.getId(),
                "previousRole", previousRole.name(),
                "newRole", newRole.name()
        ));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private Role parseRoleFilter(String roleValue) {
        if (roleValue == null || roleValue.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "role 필터는 ROLE_USER 또는 ROLE_ADMIN 이어야 합니다.");
        }
    }

    private OAuthProvider parseProviderFilter(String providerValue) {
        if (providerValue == null || providerValue.isBlank()) {
            return null;
        }
        try {
            return OAuthProvider.valueOf(providerValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "provider 필터가 올바르지 않습니다.");
        }
    }
}
