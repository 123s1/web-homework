package com.museum.reservation.dto;

import java.time.LocalDateTime;

public record MuseumInfoResponse(
        Long id,
        String name,
        String address,
        String openInfo,
        String rules,
        Integer status,
        LocalDateTime updatedAt
) {
}
