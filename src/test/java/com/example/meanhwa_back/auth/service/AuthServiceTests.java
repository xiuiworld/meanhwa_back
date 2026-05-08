package com.example.meanhwa_back.auth.service;

import java.util.List;

import com.example.meanhwa_back.auth.dto.DevLoginRequest;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTests {

    @Test
    void devLoginIsBlockedInProdProfile() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        AuthService authService = new AuthService(
                mock(UserRepository.class),
                mock(TokenService.class),
                environment,
                List.of(),
                mock(TransactionTemplate.class)
        );

        assertThatThrownBy(() -> authService.devLogin(new DevLoginRequest(
                "dev-user",
                "dev@example.com",
                "Dev User",
                "ROLE_USER"
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER));
    }
}
