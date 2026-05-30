package com.museum.reservation.dto;

import java.util.List;

public record AdminSummaryResponse(
        Long visitorCount,
        Long reservationCount,
        Long totalCapacity,
        Long bookedCount,
        Long remaining,
        List<SlotSummaryResponse> slotSummary
) {
}
