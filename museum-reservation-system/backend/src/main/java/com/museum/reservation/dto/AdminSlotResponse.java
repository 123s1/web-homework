package com.museum.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminSlotResponse(
        Long id,
        Long activityId,
        LocalDate visitDate,
        String slotName,
        LocalTime startTime,
        LocalTime endTime,
        Integer totalCapacity,
        Integer bookedCount,
        Integer remaining,
        Integer enabled,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
