package com.museum.reservation.repository;

import com.museum.reservation.entity.AdminUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AdminUser> findByUsername(String username) {
        List<AdminUser> adminUsers = jdbcTemplate.query("""
                        SELECT id, username, password, role, status
                        FROM admin_user
                        WHERE username = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> new AdminUser(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getInt("status")
                ),
                username);
        return adminUsers.stream().findFirst();
    }
}
