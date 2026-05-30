package com.museum.reservation.repository;

import com.museum.reservation.dto.AdminReservationResponse;
import com.museum.reservation.dto.MyReservationResponse;
import com.museum.reservation.entity.ReservationRecord;
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
public class ReservationRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsSuccessByIdCardAndVisitDate(String idCard, LocalDate visitDate) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM reservation_record
                        WHERE id_card = ?
                          AND visit_date = ?
                          AND status = 'SUCCESS'
                        """,
                Integer.class,
                idCard,
                Date.valueOf(visitDate));
        return count != null && count > 0;
    }

    public Long insert(String reservationNo, Long visitorId, Long activityId, Long slotId, String idCard, String phone, LocalDate visitDate, String slotName, String qrContent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_record (reservation_no, visitor_id, activity_id, slot_id, id_card, phone, visit_date, slot_name, status, qr_content)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SUCCESS', ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, reservationNo);
            preparedStatement.setLong(2, visitorId);
            preparedStatement.setLong(3, activityId);
            preparedStatement.setLong(4, slotId);
            preparedStatement.setString(5, idCard);
            preparedStatement.setString(6, phone);
            preparedStatement.setDate(7, Date.valueOf(visitDate));
            preparedStatement.setString(8, slotName);
            preparedStatement.setString(9, qrContent);
            return preparedStatement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public Optional<ReservationRecord> findById(Long id) {
        List<ReservationRecord> records = jdbcTemplate.query("""
                        SELECT id, reservation_no, visitor_id, activity_id, slot_id, id_card, phone, visit_date, slot_name, status, qr_content, created_at
                        FROM reservation_record
                        WHERE id = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    return new ReservationRecord(
                            rs.getLong("id"),
                            rs.getString("reservation_no"),
                            rs.getLong("visitor_id"),
                            rs.getLong("activity_id"),
                            rs.getLong("slot_id"),
                            rs.getString("id_card"),
                            rs.getString("phone"),
                            rs.getDate("visit_date").toLocalDate(),
                            rs.getString("slot_name"),
                            rs.getString("status"),
                            rs.getString("qr_content"),
                            createdAt == null ? null : createdAt.toLocalDateTime()
                    );
                },
                id);
        return records.stream().findFirst();
    }

    public List<MyReservationResponse> findMyReservationsByIdCard(String idCard) {
        return jdbcTemplate.query("""
                        SELECT r.id,
                               r.reservation_no,
                               v.name AS visitor_name,
                               r.id_card,
                               r.phone,
                               r.visit_date,
                               r.slot_name,
                               r.status,
                               r.qr_content,
                               r.created_at
                        FROM reservation_record r
                        INNER JOIN visitor v ON r.visitor_id = v.id
                        WHERE r.id_card = ?
                        ORDER BY r.visit_date DESC, r.created_at DESC, r.id DESC
                        """,
                (rs, rowNum) -> {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    return new MyReservationResponse(
                            rs.getLong("id"),
                            rs.getString("reservation_no"),
                            rs.getString("visitor_name"),
                            rs.getString("id_card"),
                            rs.getString("phone"),
                            rs.getDate("visit_date").toLocalDate(),
                            rs.getString("slot_name"),
                            rs.getString("status"),
                            rs.getString("qr_content"),
                            createdAt == null ? null : createdAt.toLocalDateTime()
                    );
                },
                idCard);
    }

    public List<AdminReservationResponse> findAdminReservations(LocalDate visitDate, Long slotId, String idCard, String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT r.id,
                       r.reservation_no,
                       v.name AS visitor_name,
                       r.id_card,
                       r.phone,
                       r.visit_date,
                       r.slot_name,
                       r.status,
                       r.created_at
                FROM reservation_record r
                INNER JOIN visitor v ON r.visitor_id = v.id
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (visitDate != null) {
            sql.append(" AND r.visit_date = ?");
            params.add(Date.valueOf(visitDate));
        }
        if (slotId != null) {
            sql.append(" AND r.slot_id = ?");
            params.add(slotId);
        }
        if (idCard != null && !idCard.isBlank()) {
            sql.append(" AND r.id_card = ?");
            params.add(idCard.toUpperCase());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY r.created_at DESC, r.id DESC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Timestamp createdAt = rs.getTimestamp("created_at");
            return new AdminReservationResponse(
                    rs.getLong("id"),
                    rs.getString("reservation_no"),
                    rs.getString("visitor_name"),
                    rs.getString("id_card"),
                    rs.getString("phone"),
                    rs.getDate("visit_date").toLocalDate(),
                    rs.getString("slot_name"),
                    rs.getString("status"),
                    createdAt == null ? null : createdAt.toLocalDateTime()
            );
        }, params.toArray());
    }
}
