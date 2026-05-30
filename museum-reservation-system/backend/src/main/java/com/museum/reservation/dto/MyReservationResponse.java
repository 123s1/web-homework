package com.museum.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyReservationResponse(
        Long reservationId,
        String reservationNo,
        String visitorName,
        String idCard,
        String phone,
        LocalDate visitDate,
        String slotName,
        String status,
        String qrContent,
        LocalDateTime createdAt
) {
}
