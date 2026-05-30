package com.museum.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminActivityResponse(
        Long id,
        String activityName,
        LocalDate visitDate,
        Integer dailyCapacity,
        Integer personLimit,
        LocalDateTime bookingStart,
        LocalDateTime bookingEnd,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
