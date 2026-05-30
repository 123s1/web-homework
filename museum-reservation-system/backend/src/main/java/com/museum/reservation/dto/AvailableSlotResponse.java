package com.museum.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AvailableSlotResponse(
        Long slotId,
        Long activityId,
        String activityName,
        LocalDate visitDate,
        String slotName,
        LocalTime startTime,
        LocalTime endTime,
        Integer totalCapacity,
        Integer bookedCount,
        Integer remaining,
        Integer enabled,
        String activityStatus,
        LocalDateTime bookingStart,
        LocalDateTime bookingEnd
) {
}
