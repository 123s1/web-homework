package com.museum.reservation.service;

import com.museum.reservation.dto.AdminSystemStatusResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SystemService {

    private final JdbcTemplate jdbcTemplate;

    public SystemService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> health() {
        Integer databaseResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("database", databaseResult != null && databaseResult == 1 ? "UP" : "DOWN");
        result.put("time", LocalDateTime.now());
        return result;
    }

    public AdminSystemStatusResponse adminStatus() {
        Integer databaseResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        Long totalRequestCount = countRequests(null);
        Long successRequestCount = countRequests(1);
        Long failedRequestCount = countRequests(0);
        Timestamp lastRequestTime = jdbcTemplate.queryForObject("SELECT MAX(created_at) FROM request_log", Timestamp.class);
        return new AdminSystemStatusResponse(
                "UP",
                databaseResult != null && databaseResult == 1 ? "UP" : "DOWN",
                totalRequestCount,
                successRequestCount,
                failedRequestCount,
                lastRequestTime == null ? null : lastRequestTime.toLocalDateTime()
        );
    }

    private Long countRequests(Integer success) {
        Long count;
        if (success == null) {
            count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM request_log", Long.class);
        } else {
            count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM request_log WHERE success = ?", Long.class, success);
        }
        return count == null ? 0L : count;
    }
}
