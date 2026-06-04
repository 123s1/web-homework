package com.museum.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约时段与所属活动的合并视图，由一次 JOIN 查询填充，
 * 避免预约流程中分两次查询 slot 与 activity。
 */
public record SlotWithActivity(
        Long slotId,
        Long activityId,
        LocalDate visitDate,
        String slotName,
        LocalTime startTime,
        LocalTime endTime,
        Integer totalCapacity,
        Integer bookedCount,
        Integer enabled,
        Integer version,
        String activityStatus,
        LocalDateTime bookingStart,
        LocalDateTime bookingEnd,
        Integer personLimit
) {
    public int remaining() {
        int capacity = totalCapacity == null ? 0 : totalCapacity;
        int booked = bookedCount == null ? 0 : bookedCount;
        return capacity - booked;
    }
}
