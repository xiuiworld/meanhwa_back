package com.example.meanhwa_back.log.repository;

import java.util.List;

import com.example.meanhwa_back.log.domain.ActionLog;
import com.example.meanhwa_back.log.domain.ActionType;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    long countByActionType(ActionType actionType);

    List<ActionLog> findByActionTypeOrderByIdDesc(ActionType actionType);
}
