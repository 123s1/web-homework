package com.museum.reservation.dto;

import java.time.LocalDate;

public record SlotSummaryResponse(
        Long slotId,
        String slotName,
        LocalDate visitDate,
        Long totalCapacity,
        Long bookedCount,
        Long remaining
) {
}
