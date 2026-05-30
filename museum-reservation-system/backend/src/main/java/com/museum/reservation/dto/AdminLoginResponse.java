package com.museum.reservation.dto;

public record AdminLoginResponse(
        Long adminId,
        String username,
        String role
) {
}
