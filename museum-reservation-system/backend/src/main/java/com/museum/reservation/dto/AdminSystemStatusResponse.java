package com.museum.reservation.dto;

import java.time.LocalDateTime;

public record AdminSystemStatusResponse(
        String systemStatus,
        String databaseStatus,
        Long totalRequestCount,
        Long successRequestCount,
        Long failedRequestCount,
        LocalDateTime lastRequestTime
) {
}
