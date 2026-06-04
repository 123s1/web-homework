package com.museum.reservation.service;

import com.museum.reservation.dto.AvailableSlotResponse;
import com.museum.reservation.dto.MuseumInfoResponse;
import com.museum.reservation.dto.NoticeResponse;
import com.museum.reservation.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MuseumService {

    private static final long MUSEUM_INFO_TTL_MS = 5 * 60 * 1000L;
    private static final long NOTICES_TTL_MS = 2 * 60 * 1000L;
    private static final long SLOTS_TTL_MS = 3 * 1000L;

    private final JdbcTemplate jdbcTemplate;

    private volatile MuseumInfoResponse cachedMuseumInfo;
    private volatile long museumInfoExpireAt = 0L;

    private volatile List<NoticeResponse> cachedNotices;
    private volatile long noticesExpireAt = 0L;

    private volatile List<AvailableSlotResponse> cachedAllSlots;
    private volatile long allSlotsExpireAt = 0L;

    public MuseumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MuseumInfoResponse getMuseumInfo() {
        long now = System.currentTimeMillis();
        MuseumInfoResponse cached = cachedMuseumInfo;
        if (cached != null && now < museumInfoExpireAt) {
            return cached;
        }
        MuseumInfoResponse info = queryMuseumInfoFromDb();
        cachedMuseumInfo = info;
        museumInfoExpireAt = now + MUSEUM_INFO_TTL_MS;
        return info;
    }

    public List<NoticeResponse> listEnabledNotices() {
        long now = System.currentTimeMillis();
        List<NoticeResponse> cached = cachedNotices;
        if (cached != null && now < noticesExpireAt) {
            return cached;
        }
        List<NoticeResponse> notices = queryEnabledNoticesFromDb();
        cachedNotices = notices;
        noticesExpireAt = now + NOTICES_TTL_MS;
        return notices;
    }

    public List<AvailableSlotResponse> listAvailableSlots(LocalDate visitDate) {
        // booked_count 实时变化，仅对无日期参数的全量查询做 3 秒短缓存；
        // 带具体日期的查询直接落库，保证余量准确。
        if (visitDate != null) {
            return queryAvailableSlotsFromDb(visitDate);
        }
        long now = System.currentTimeMillis();
        List<AvailableSlotResponse> cached = cachedAllSlots;
        if (cached != null && now < allSlotsExpireAt) {
            return cached;
        }
        List<AvailableSlotResponse> slots = queryAvailableSlotsFromDb(null);
        cachedAllSlots = slots;
        allSlotsExpireAt = now + SLOTS_TTL_MS;
        return slots;
    }

    private MuseumInfoResponse queryMuseumInfoFromDb() {
        List<MuseumInfoResponse> results = jdbcTemplate.query("""
                        SELECT id, name, address, open_info, rules, status, updated_at
                        FROM museum_info
                        WHERE status = 1
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
        if (results.isEmpty()) {
            throw new BusinessException("未配置可展示的场馆信息");
        }
        return results.get(0);
    }

    private List<NoticeResponse> queryEnabledNoticesFromDb() {
        return jdbcTemplate.query("""
                        SELECT id, title, content, type, enabled, created_at
                        FROM notice
                        WHERE enabled = 1
                        ORDER BY created_at DESC, id DESC
                        """,
                (rs, rowNum) -> {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    return new NoticeResponse(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("type"),
                            rs.getInt("enabled"),
                            createdAt == null ? null : createdAt.toLocalDateTime()
                    );
                });
    }

    private List<AvailableSlotResponse> queryAvailableSlotsFromDb(LocalDate visitDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id AS slot_id,
                       a.id AS activity_id,
                       a.activity_name,
                       s.visit_date,
                       s.slot_name,
                       s.start_time,
                       s.end_time,
                       s.total_capacity,
                       s.booked_count,
                       s.enabled,
                       a.status AS activity_status,
                       a.booking_start,
                       a.booking_end
                FROM reservation_slot s
                INNER JOIN reservation_activity a ON s.activity_id = a.id
                WHERE a.status = 'OPEN'
                  AND s.enabled = 1
                  AND s.visit_date >= CURRENT_DATE
                """);
        List<Object> params = new ArrayList<>();
        if (visitDate != null) {
            sql.append(" AND s.visit_date = ?");
            params.add(Date.valueOf(visitDate));
        }
        sql.append(" ORDER BY s.visit_date ASC, s.start_time ASC, s.id ASC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Time startTime = rs.getTime("start_time");
            Time endTime = rs.getTime("end_time");
            Timestamp bookingStart = rs.getTimestamp("booking_start");
            Timestamp bookingEnd = rs.getTimestamp("booking_end");
            int totalCapacity = rs.getInt("total_capacity");
            int bookedCount = rs.getInt("booked_count");
            return new AvailableSlotResponse(
                    rs.getLong("slot_id"),
                    rs.getLong("activity_id"),
                    rs.getString("activity_name"),
                    rs.getDate("visit_date").toLocalDate(),
                    rs.getString("slot_name"),
                    startTime == null ? null : startTime.toLocalTime(),
                    endTime == null ? null : endTime.toLocalTime(),
                    totalCapacity,
                    bookedCount,
                    totalCapacity - bookedCount,
                    rs.getInt("enabled"),
                    rs.getString("activity_status"),
                    bookingStart == null ? null : bookingStart.toLocalDateTime(),
                    bookingEnd == null ? null : bookingEnd.toLocalDateTime()
            );
        }, params.toArray());
    }
}
