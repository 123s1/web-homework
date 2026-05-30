package com.museum.reservation.entity;

public record Visitor(
        Long id,
        String name,
        String idCard,
        String phone
) {
}
