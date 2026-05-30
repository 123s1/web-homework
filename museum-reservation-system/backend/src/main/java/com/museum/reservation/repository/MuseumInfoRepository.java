package com.museum.reservation.repository;

import com.museum.reservation.dto.MuseumInfoResponse;
import com.museum.reservation.dto.MuseumInfoUpdateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MuseumInfoRepository {

    private final JdbcTemplate jdbcTemplate;

    public MuseumInfoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<MuseumInfoResponse> findFirst() {
        List<MuseumInfoResponse> results = jdbcTemplate.query("""
                        SELECT id, name, address, open_info, rules, status, updated_at
                        FROM museum_info
                        ORDER BY id
                        LIMIT 1
                        """,
                (rs, rowNum) -> new MuseumInfoResponse(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("open_info"),
                        rs.getString("rules"),
                        rs.getInt("status"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ));
        return results.stream().findFirst();
    }

    public int updateById(Long id, MuseumInfoUpdateRequest request) {
        return jdbcTemplate.update("""
                        UPDATE museum_info
                        SET name = ?,
                            address = ?,
                            open_info = ?,
                            rules = ?,
                            status = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                request.name(),
                request.address(),
                request.openInfo(),
                request.rules(),
                request.status(),
                id);
    }
}
