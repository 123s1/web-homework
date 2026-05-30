package com.museum.reservation.dto;

public record VisitorLoginResponse(
        Long visitorId,
        String name,
        String idCard,
        String phone
) {
}
