package com.example.meanhwa_back.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.meanhwa_back.user.domain.Role;

/**
 * Spring Security 설정.
 * JWT 기반 stateless 인증을 사용하며, 공개 API·관리자 API 경로를 구분한다.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final boolean h2ConsoleEnabled;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            Environment environment
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.h2ConsoleEnabled = environment.acceptsProfiles(Profiles.of("local", "test"));
    }
/**
 * 공개 API, 인증 API, 관리자 API의 접근 정책과 JWT 필터를 구성한다.
 */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll();
                    if (h2ConsoleEnabled) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }
                    auth
                            // 비로그인 조회: 꽃 사전, 태그
                            .requestMatchers(HttpMethod.GET, "/api/v1/flowers", "/api/v1/flowers/*").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/tags").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/curation/results").permitAll()
                            // 비로그인 쓰기: 인증, 큐레이션 결과 클릭 로그
                            .requestMatchers(
                                    HttpMethod.POST,
                                    "/api/v1/auth/**",
                                    "/api/v1/action-logs/curation-result-click"
                            ).permitAll()
                            // 메시지 생성: JWT 필수 (userId 기준 Rate Limit 적용)
                            .requestMatchers(HttpMethod.POST, "/api/v1/messages/generate").authenticated()
                            .requestMatchers("/api/v1/admin/**").hasAuthority(Role.ROLE_ADMIN.name())
                            .requestMatchers("/api/v1/users/**").authenticated()
                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
