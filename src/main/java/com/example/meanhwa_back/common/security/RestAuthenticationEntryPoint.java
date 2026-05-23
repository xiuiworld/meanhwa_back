package com.example.meanhwa_back.common.security;

import java.io.IOException;

import com.example.meanhwa_back.common.error.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** 미인증 요청 시 401 JSON 응답. */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final SecurityErrorWriter securityErrorWriter;

    public RestAuthenticationEntryPoint(SecurityErrorWriter securityErrorWriter) {
        this.securityErrorWriter = securityErrorWriter;
    }

    /**
     * 인증 실패 상황을 JSON 오류 응답으로 기록한다.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        securityErrorWriter.write(response, ErrorCode.UNAUTHORIZED);
    }
}
