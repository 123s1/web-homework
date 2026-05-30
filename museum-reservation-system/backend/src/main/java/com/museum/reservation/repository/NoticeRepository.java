package com.museum.reservation.repository;

import com.museum.reservation.dto.AdminNoticeResponse;
import com.museum.reservation.dto.NoticeSaveRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class NoticeRepository {

    private final JdbcTemplate jdbcTemplate;

    public NoticeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminNoticeResponse> findAll(Integer enabled, String type) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, title, content, type, enabled, created_at, updated_at
                FROM notice
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (enabled != null) {
            sql.append(" AND enabled = ?");
            params.add(enabled);
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        sql.append(" ORDER BY created_at DESC, id DESC");
        return jdbcTemplate.query(sql.toString(), this::mapNotice, params.toArray());
    }

    public Optional<AdminNoticeResponse> findById(Long id) {
        List<AdminNoticeResponse> results = jdbcTemplate.query("""
                        SELECT id, title, content, type, enabled, created_at, updated_at
                        FROM notice
                        WHERE id = ?
                        LIMIT 1
                        """,
                this::mapNotice,
                id);
        return results.stream().findFirst();
    }

    public Long insert(NoticeSaveRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO notice (title, content, type, enabled)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, request.title());
            preparedStatement.setString(2, request.content());
            preparedStatement.setString(3, request.type());
            preparedStatement.setInt(4, request.enabled());
            return preparedStatement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int updateById(Long id, NoticeSaveRequest request) {
        return jdbcTemplate.update("""
                        UPDATE notice
                        SET title = ?,
                            content = ?,
                            type = ?,
                            enabled = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                request.title(),
                request.content(),
                request.type(),
                request.enabled(),
                id);
    }

    public int disableById(Long id) {
        return jdbcTemplate.update("""
                        UPDATE notice
                        SET enabled = 0,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                id);
    }

    private AdminNoticeResponse mapNotice(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new AdminNoticeResponse(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("type"),
                rs.getInt("enabled"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime()
        );
    }
}
