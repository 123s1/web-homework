package com.museum.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminReservationResponse(
        Long reservationId,
        String reservationNo,
        String visitorName,
        String idCard,
        String phone,
        LocalDate visitDate,
        String slotName,
        String status,
        LocalDateTime createdAt
) {
}
