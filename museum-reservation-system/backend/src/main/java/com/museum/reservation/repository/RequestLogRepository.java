package com.museum.reservation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RequestLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public RequestLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String requestType, boolean success, String message) {
        jdbcTemplate.update("""
                        INSERT INTO request_log (request_type, success, message)
                        VALUES (?, ?, ?)
                        """,
                truncate(requestType, 50),
                success ? 1 : 0,
                truncate(message, 255));
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
