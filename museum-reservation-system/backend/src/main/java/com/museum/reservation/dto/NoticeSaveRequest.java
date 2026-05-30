package com.museum.reservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record NoticeSaveRequest(
        @NotBlank(message = "公告标题不能为空")
        String title,
        @NotBlank(message = "公告内容不能为空")
        String content,
        @NotBlank(message = "公告类型不能为空")
        @Pattern(regexp = "NORMAL|EXHIBITION|CLOSE|RULE", message = "公告类型不正确")
        String type,
        @NotNull(message = "启用状态不能为空")
        @Min(value = 0, message = "启用状态不正确")
        @Max(value = 1, message = "启用状态不正确")
        Integer enabled
) {
}
