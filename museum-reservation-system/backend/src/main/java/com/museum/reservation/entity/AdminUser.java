package com.museum.reservation.entity;

public record AdminUser(
        Long id,
        String username,
        String password,
        String role,
        Integer status
) {
}
