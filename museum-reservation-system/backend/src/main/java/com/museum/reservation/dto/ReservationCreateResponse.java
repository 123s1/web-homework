package com.museum.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationCreateResponse(
        Long reservationId,
        String reservationNo,
        String visitorName,
        String idCard,
        String phone,
        LocalDate visitDate,
        String slotName,
        LocalTime startTime,
        LocalTime endTime,
        String qrContent,
        LocalDateTime createdAt
) {
}
