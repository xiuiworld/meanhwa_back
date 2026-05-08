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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActionLogService {
    private static final Logger log = LoggerFactory.getLogger(ActionLogService.class);

    private final ActionLogRepository actionLogRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ObjectMapper objectMapper;

    public ActionLogService(
            ActionLogRepository actionLogRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            ObjectMapper objectMapper
    ) {
        this.actionLogRepository = actionLogRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.objectMapper = objectMapper;
    }

    public void record(ActionType actionType, Map<String, Object> payload) {
        try {
            User user = authenticatedUserProvider.getCurrentUserOrNull();
            Long userId = user == null ? null : user.getId();
            save(userId, actionType, toJson(payload));
        } catch (Exception exception) {
            log.warn("Failed to record action log. actionType={}", actionType, exception);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long userId, ActionType actionType, String actionData) {
        actionLogRepository.save(new ActionLog(userId, actionType, actionData));
    }

    private String toJson(Map<String, Object> payload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
    }
}
