package com.museum.reservation.repository;

import com.museum.reservation.dto.AdminSlotResponse;
import com.museum.reservation.dto.SlotCreateRequest;
import com.museum.reservation.dto.SlotUpdateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public SlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminSlotResponse> findAll(Long activityId, LocalDate visitDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, activity_id, visit_date, slot_name, start_time, end_time, total_capacity, booked_count, enabled, version, created_at, updated_at
                FROM reservation_slot
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (activityId != null) {
            sql.append(" AND activity_id = ?");
            params.add(activityId);
        }
        if (visitDate != null) {
            sql.append(" AND visit_date = ?");
            params.add(Date.valueOf(visitDate));
        }
        sql.append(" ORDER BY visit_date ASC, start_time ASC, id ASC");
        return jdbcTemplate.query(sql.toString(), this::mapSlot, params.toArray());
    }

    public Optional<AdminSlotResponse> findById(Long id) {
        List<AdminSlotResponse> results = jdbcTemplate.query("""
                        SELECT id, activity_id, visit_date, slot_name, start_time, end_time, total_capacity, booked_count, enabled, version, created_at, updated_at
                        FROM reservation_slot
                        WHERE id = ?
                        LIMIT 1
                        """,
                this::mapSlot,
                id);
        return results.stream().findFirst();
    }

    public boolean existsByActivityAndTime(Long activityId, LocalTime startTime, LocalTime endTime, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM reservation_slot WHERE activity_id = ? AND start_time = ? AND end_time = ?";
        List<Object> params = new ArrayList<>();
        params.add(activityId);
        params.add(Time.valueOf(startTime));
        params.add(Time.valueOf(endTime));
        if (excludeId != null) {
            sql += " AND id <> ?";
            params.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
        return count != null && count > 0;
    }

    public Integer sumEnabledCapacity(Long activityId, Long excludeId) {
        String sql = "SELECT COALESCE(SUM(total_capacity), 0) FROM reservation_slot WHERE activity_id = ? AND enabled = 1";
        List<Object> params = new ArrayList<>();
        params.add(activityId);
        if (excludeId != null) {
            sql += " AND id <> ?";
            params.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
        return count == null ? 0 : count;
    }

    public Long insert(SlotCreateRequest request, LocalDate visitDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_slot (activity_id, visit_date, slot_name, start_time, end_time, total_capacity, enabled)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, request.activityId());
            preparedStatement.setDate(2, Date.valueOf(visitDate));
            preparedStatement.setString(3, request.slotName());
            preparedStatement.setTime(4, Time.valueOf(request.startTime()));
            preparedStatement.setTime(5, Time.valueOf(request.endTime()));
            preparedStatement.setInt(6, request.totalCapacity());
            preparedStatement.setInt(7, request.enabled());
            return preparedStatement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int updateById(Long id, SlotUpdateRequest request) {
        return jdbcTemplate.update("""
                        UPDATE reservation_slot
                        SET slot_name = ?,
                            start_time = ?,
                            end_time = ?,
                            total_capacity = ?,
                            enabled = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                request.slotName(),
                Time.valueOf(request.startTime()),
                Time.valueOf(request.endTime()),
                request.totalCapacity(),
                request.enabled(),
                id);
    }

    public int increaseBookedCount(Long id) {
        return jdbcTemplate.update("""
                        UPDATE reservation_slot
                        SET booked_count = booked_count + 1,
                            version = version + 1,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                          AND enabled = 1
                          AND booked_count < total_capacity
                        """,
                id);
    }

    private AdminSlotResponse mapSlot(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Time startTime = rs.getTime("start_time");
        Time endTime = rs.getTime("end_time");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        int totalCapacity = rs.getInt("total_capacity");
        int bookedCount = rs.getInt("booked_count");
        return new AdminSlotResponse(
                rs.getLong("id"),
                rs.getLong("activity_id"),
                rs.getDate("visit_date").toLocalDate(),
                rs.getString("slot_name"),
                startTime == null ? null : startTime.toLocalTime(),
                endTime == null ? null : endTime.toLocalTime(),
                totalCapacity,
                bookedCount,
                totalCapacity - bookedCount,
                rs.getInt("enabled"),
                rs.getInt("version"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime()
        );
    }
}
