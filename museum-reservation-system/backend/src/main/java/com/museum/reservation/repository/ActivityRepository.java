package com.museum.reservation.repository;

import com.museum.reservation.dto.ActivitySaveRequest;
import com.museum.reservation.dto.AdminActivityResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ActivityRepository {

    private final JdbcTemplate jdbcTemplate;

    public ActivityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminActivityResponse> findAll(String status, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, activity_name, visit_date, daily_capacity, person_limit, booking_start, booking_end, status, created_at, updated_at
                FROM reservation_activity
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (startDate != null) {
            sql.append(" AND visit_date >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND visit_date <= ?");
            params.add(Date.valueOf(endDate));
        }
        sql.append(" ORDER BY visit_date ASC, id ASC");
        return jdbcTemplate.query(sql.toString(), this::mapActivity, params.toArray());
    }

    public Optional<AdminActivityResponse> findById(Long id) {
        List<AdminActivityResponse> results = jdbcTemplate.query("""
                        SELECT id, activity_name, visit_date, daily_capacity, person_limit, booking_start, booking_end, status, created_at, updated_at
                        FROM reservation_activity
                        WHERE id = ?
                        LIMIT 1
                        """,
                this::mapActivity,
                id);
        return results.stream().findFirst();
    }

    public boolean existsByVisitDate(LocalDate visitDate, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM reservation_activity WHERE visit_date = ?";
        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(visitDate));
        if (excludeId != null) {
            sql += " AND id <> ?";
            params.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
        return count != null && count > 0;
    }

    public Integer sumSlotBookedCount(Long activityId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(booked_count), 0)
                        FROM reservation_slot
                        WHERE activity_id = ?
                        """,
                Integer.class,
                activityId);
        return count == null ? 0 : count;
    }

    public Long insert(ActivitySaveRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_activity (activity_name, visit_date, daily_capacity, person_limit, booking_start, booking_end, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, request.activityName());
            preparedStatement.setDate(2, Date.valueOf(request.visitDate()));
            preparedStatement.setInt(3, request.dailyCapacity());
            preparedStatement.setInt(4, request.personLimit());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(request.bookingStart()));
            preparedStatement.setTimestamp(6, Timestamp.valueOf(request.bookingEnd()));
            preparedStatement.setString(7, request.status());
            return preparedStatement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int updateById(Long id, ActivitySaveRequest request) {
        return jdbcTemplate.update("""
                        UPDATE reservation_activity
                        SET activity_name = ?,
                            visit_date = ?,
                            daily_capacity = ?,
                            person_limit = ?,
                            booking_start = ?,
                            booking_end = ?,
                            status = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                request.activityName(),
                Date.valueOf(request.visitDate()),
                request.dailyCapacity(),
                request.personLimit(),
                Timestamp.valueOf(request.bookingStart()),
                Timestamp.valueOf(request.bookingEnd()),
                request.status(),
                id);
    }

    private AdminActivityResponse mapActivity(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp bookingStart = rs.getTimestamp("booking_start");
        Timestamp bookingEnd = rs.getTimestamp("booking_end");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new AdminActivityResponse(
                rs.getLong("id"),
                rs.getString("activity_name"),
                rs.getDate("visit_date").toLocalDate(),
                rs.getInt("daily_capacity"),
                rs.getInt("person_limit"),
                bookingStart == null ? null : bookingStart.toLocalDateTime(),
                bookingEnd == null ? null : bookingEnd.toLocalDateTime(),
                rs.getString("status"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime()
        );
    }
}
