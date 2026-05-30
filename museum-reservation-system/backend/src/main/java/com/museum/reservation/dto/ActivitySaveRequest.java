package com.museum.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ActivitySaveRequest(
        @NotBlank(message = "预约活动名称不能为空")
        String activityName,
        @NotNull(message = "参观日期不能为空")
        LocalDate visitDate,
        @NotNull(message = "每日总名额不能为空")
        @Min(value = 1, message = "每日总名额必须大于 0")
        Integer dailyCapacity,
        @NotNull(message = "单人预约上限不能为空")
        @Min(value = 1, message = "单人预约上限必须大于 0")
        Integer personLimit,
        @NotNull(message = "预约开始时间不能为空")
        LocalDateTime bookingStart,
        @NotNull(message = "预约结束时间不能为空")
        LocalDateTime bookingEnd,
        @NotBlank(message = "活动状态不能为空")
        @Pattern(regexp = "DRAFT|OPEN|CLOSED|DISABLED", message = "活动状态不正确")
        String status
) {
}
