package com.museum.reservation.dto;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        String type,
        Integer enabled,
        LocalDateTime createdAt
) {
}
