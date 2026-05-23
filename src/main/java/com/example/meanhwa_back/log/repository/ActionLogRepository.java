package com.example.meanhwa_back.log.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.example.meanhwa_back.log.domain.ActionLog;
import com.example.meanhwa_back.log.domain.ActionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    long countByActionType(ActionType actionType);

    List<ActionLog> findByActionTypeOrderByIdDesc(ActionType actionType);

    long countByActionTypeAndCreatedAtBetween(ActionType actionType, LocalDateTime from, LocalDateTime to);

    List<ActionLog> findByActionTypeInAndCreatedAtBetween(
            Collection<ActionType> actionTypes,
            LocalDateTime from,
            LocalDateTime to
    );

    List<ActionLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
            select l.actionData as actionData
            from ActionLog l
            where l.actionType in :actionTypes
              and l.createdAt between :from and :to
            """)
    List<ActionDataView> findActionDataByActionTypeInAndCreatedAtBetween(
            @Param("actionTypes") Collection<ActionType> actionTypes,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            select l.userId as userId, l.createdAt as createdAt
            from ActionLog l
            where l.createdAt between :from and :to
              and l.userId is not null
            """)
    List<UserActivityView> findUserActivityByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /** JSON actionData만 필요한 관리자 통계용 projection. */
    interface ActionDataView {
        String getActionData();
    }

    /** 일별 활성 사용자 집계에 필요한 최소 컬럼 projection. */
    interface UserActivityView {
        Long getUserId();

        LocalDateTime getCreatedAt();
    }
}
