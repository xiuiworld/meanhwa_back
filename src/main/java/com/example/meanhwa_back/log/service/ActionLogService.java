package com.example.meanhwa_back.log.service;

import java.util.Map;

import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.log.domain.ActionLog;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.repository.ActionLogRepository;
import com.example.meanhwa_back.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 사용자 행동 로그를 별도 트랜잭션(REQUIRES_NEW)으로 저장한다.
 * 로그 실패가 본 요청을 롤백하지 않도록 예외를 삼킨다.
 *
 * <p>대부분의 API는 서비스/컨트롤러에 {@link com.example.meanhwa_back.log.aop.LogAction} 을 선언하고
 * {@link com.example.meanhwa_back.log.aop.ActionLogAspect} 가 본 메서드를 호출한다.
 * 직접 {@link #record} 를 호출할 필요는 특수 케이스에만 남긴다.
 */
@Service
public class ActionLogService {
    private static final Logger log = LoggerFactory.getLogger(ActionLogService.class);

    private final ActionLogRepository actionLogRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public ActionLogService(
            ActionLogRepository actionLogRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager
    ) {
        this.actionLogRepository = actionLogRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
    }

    public void record(ActionType actionType, Map<String, Object> payload) {
        try {
            User user = authenticatedUserProvider.getCurrentUserOrNull();
            Long userId = user == null ? null : user.getId();
            String actionData = toJson(payload);
            transactionTemplate.executeWithoutResult(status ->
                    actionLogRepository.save(new ActionLog(userId, actionType, actionData))
            );
        } catch (Exception exception) {
            log.warn("Failed to record action log. actionType={}", actionType, exception);
        }
    }

    private String toJson(Map<String, Object> payload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
    }
}
