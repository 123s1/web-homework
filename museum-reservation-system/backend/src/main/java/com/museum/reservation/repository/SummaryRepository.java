package com.museum.reservation.repository;

import com.museum.reservation.dto.AdminSummaryResponse;
import com.museum.reservation.dto.SlotSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SummaryRepository {

    private final JdbcTemplate jdbcTemplate;

    public SummaryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminSummaryResponse getSummary(LocalDate visitDate) {
        Long visitorCount = countVisitors(visitDate);
        Long reservationCount = countReservations(visitDate);
        Long totalCapacity = sumSlotColumn("total_capacity", visitDate);
        Long bookedCount = sumSlotColumn("booked_count", visitDate);
        Long remaining = totalCapacity - bookedCount;
        List<SlotSummaryResponse> slotSummary = listSlotSummary(visitDate);
        return new AdminSummaryResponse(visitorCount, reservationCount, totalCapacity, bookedCount, remaining, slotSummary);
    }

    private Long countVisitors(LocalDate visitDate) {
        if (visitDate == null) {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visitor", Long.class);
            return count == null ? 0L : count;
        }
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(DISTINCT visitor_id)
                        FROM reservation_record
                        WHERE visit_date = ?
                          AND status = 'SUCCESS'
                        """,
                Long.class,
                Date.valueOf(visitDate));
        return count == null ? 0L : count;
    }

    private Long countReservations(LocalDate visitDate) {
        if (visitDate == null) {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_record WHERE status = 'SUCCESS'", Long.class);
            return count == null ? 0L : count;
        }
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM reservation_record
                        WHERE visit_date = ?
                          AND status = 'SUCCESS'
                        """,
                Long.class,
                Date.valueOf(visitDate));
        return count == null ? 0L : count;
    }

    private Long sumSlotColumn(String columnName, LocalDate visitDate) {
        String sql = "SELECT COALESCE(SUM(" + columnName + "), 0) FROM reservation_slot WHERE enabled = 1";
        List<Object> params = new ArrayList<>();
        if (visitDate != null) {
            sql += " AND visit_date = ?";
            params.add(Date.valueOf(visitDate));
        }
        Long count = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
        return count == null ? 0L : count;
    }

    private List<SlotSummaryResponse> listSlotSummary(LocalDate visitDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, slot_name, visit_date, total_capacity, booked_count
                FROM reservation_slot
                WHERE enabled = 1
                """);
        List<Object> params = new ArrayList<>();
        if (visitDate != null) {
            sql.append(" AND visit_date = ?");
            params.add(Date.valueOf(visitDate));
        }
        sql.append(" ORDER BY visit_date ASC, start_time ASC, id ASC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            long totalCapacity = rs.getLong("total_capacity");
            long bookedCount = rs.getLong("booked_count");
            return new SlotSummaryResponse(
                    rs.getLong("id"),
                    rs.getString("slot_name"),
                    rs.getDate("visit_date").toLocalDate(),
                    totalCapacity,
                    bookedCount,
                    totalCapacity - bookedCount
            );
        }, params.toArray());
    }
}
