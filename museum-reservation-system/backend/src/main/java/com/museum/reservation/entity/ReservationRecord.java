package com.museum.reservation.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationRecord(
        Long id,
        String reservationNo,
        Long visitorId,
        Long activityId,
        Long slotId,
        String idCard,
        String phone,
        LocalDate visitDate,
        String slotName,
        String status,
        String qrContent,
        LocalDateTime createdAt
) {
}
