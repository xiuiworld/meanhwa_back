package com.example.meanhwa_back.admin.dto.statistics;

import java.time.LocalDate;

public record DailyActiveUserResponse(
        LocalDate date,
        long activeUsers
) {
}
