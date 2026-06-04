package com.museum.reservation.repository;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Repository
public class RequestLogRepository {

    private static final Logger log = LoggerFactory.getLogger(RequestLogRepository.class);

    private static final int QUEUE_CAPACITY = 10000;
    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;
    private final LinkedBlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    public RequestLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record LogEntry(String requestType, boolean success, String message) {
    }

    /**
     * 异步入队（内存操作，约 1μs），请求线程不再直接写库。
     * 队列满时丢弃该条日志，保证业务请求不被日志拖慢。
     */
    public void enqueue(String requestType, boolean success, String message) {
        LogEntry entry = new LogEntry(truncate(requestType, 50), success, truncate(message, 255));
        if (!queue.offer(entry)) {
            log.warn("请求日志队列已满，丢弃一条日志: {}", requestType);
        }
    }

    /**
     * 同步写入，保留给需要立即落库的场景。
     */
    public void insert(String requestType, boolean success, String message) {
        jdbcTemplate.update("""
                        INSERT INTO request_log (request_type, success, message)
                        VALUES (?, ?, ?)
                        """,
                truncate(requestType, 50),
                success ? 1 : 0,
                truncate(message, 255));
    }

    /**
     * 每 2 秒批量落库一次，把队列中的日志用一条 batchUpdate 写入。
     */
    @Scheduled(fixedDelay = 2000)
    public void flush() {
        if (queue.isEmpty()) {
            return;
        }
        List<LogEntry> batch = new ArrayList<>(BATCH_SIZE);
        LogEntry entry;
        while (batch.size() < BATCH_SIZE && (entry = queue.poll()) != null) {
            batch.add(entry);
        }
        if (batch.isEmpty()) {
            return;
        }
        try {
            batchInsert(batch);
        } catch (Exception exception) {
            log.warn("批量写入请求日志失败，丢弃 {} 条: {}", batch.size(), exception.getMessage());
        }
    }

    private void batchInsert(List<LogEntry> batch) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO request_log (request_type, success, message) VALUES (?, ?, ?)",
                batch,
                batch.size(),
                (ps, item) -> {
                    ps.setString(1, item.requestType());
                    ps.setInt(2, item.success() ? 1 : 0);
                    ps.setString(3, item.message());
                });
    }

    /**
     * 应用关闭前把队列剩余日志尽量落库，减少丢失。
     */
    @PreDestroy
    public void flushOnShutdown() {
        while (!queue.isEmpty()) {
            flush();
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
