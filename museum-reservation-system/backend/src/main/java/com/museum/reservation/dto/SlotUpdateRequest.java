package com.museum.reservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record SlotUpdateRequest(
        @NotBlank(message = "时段名称不能为空")
        String slotName,
        @NotNull(message = "入场开始时间不能为空")
        LocalTime startTime,
        @NotNull(message = "入场结束时间不能为空")
        LocalTime endTime,
        @NotNull(message = "时段总名额不能为空")
        @Min(value = 1, message = "时段总名额必须大于 0")
        Integer totalCapacity,
        @NotNull(message = "启用状态不能为空")
        @Min(value = 0, message = "启用状态不正确")
        @Max(value = 1, message = "启用状态不正确")
        Integer enabled
) {
}
