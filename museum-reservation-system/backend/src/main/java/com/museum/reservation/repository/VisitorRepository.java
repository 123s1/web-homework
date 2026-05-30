package com.museum.reservation.repository;

import com.museum.reservation.dto.VisitorLoginRequest;
import com.museum.reservation.entity.Visitor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class VisitorRepository {

    private final JdbcTemplate jdbcTemplate;

    public VisitorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Visitor> findByIdCard(String idCard) {
        List<Visitor> visitors = jdbcTemplate.query("""
                        SELECT id, name, id_card, phone
                        FROM visitor
                        WHERE id_card = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> new Visitor(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("id_card"),
                        rs.getString("phone")
                ),
                idCard);
        return visitors.stream().findFirst();
    }

    public void lockById(Long id) {
        jdbcTemplate.queryForObject("""
                        SELECT id
                        FROM visitor
                        WHERE id = ?
                        FOR UPDATE
                        """,
                Long.class,
                id);
    }

    public Long insert(VisitorLoginRequest request) {
        return insert(request.name(), request.idCard(), request.phone());
    }

    public Long insert(String name, String idCard, String phone) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO visitor (name, id_card, phone)
                    VALUES (?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, normalizeIdCard(idCard));
            preparedStatement.setString(3, phone);
            return preparedStatement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int updateById(Long id, VisitorLoginRequest request) {
        return updateById(id, request.name(), request.phone());
    }

    public int updateById(Long id, String name, String phone) {
        return jdbcTemplate.update("""
                        UPDATE visitor
                        SET name = ?,
                            phone = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                name,
                phone,
                id);
    }

    private String normalizeIdCard(String idCard) {
        return idCard == null ? null : idCard.toUpperCase();
    }
}
