package com.example.meanhwa_back.user.service;

import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.user.dto.UserMeResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** SecurityContext 기준 현재 로그인 사용자 정보 조회. */
@Service
@Transactional(readOnly = true)
public class UserService {
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public UserService(AuthenticatedUserProvider authenticatedUserProvider) {
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public UserMeResponse getMe() {
        return UserMeResponse.from(authenticatedUserProvider.getCurrentUser());
    }
}
