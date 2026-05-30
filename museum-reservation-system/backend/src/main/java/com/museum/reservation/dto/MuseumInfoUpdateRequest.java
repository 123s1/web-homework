package com.museum.reservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MuseumInfoUpdateRequest(
        @NotBlank(message = "场馆名称不能为空")
        String name,
        @NotBlank(message = "场馆地址不能为空")
        String address,
        @NotBlank(message = "开放时间说明不能为空")
        String openInfo,
        @NotBlank(message = "参观须知不能为空")
        String rules,
        @NotNull(message = "场馆状态不能为空")
        @Min(value = 0, message = "场馆状态不正确")
        @Max(value = 1, message = "场馆状态不正确")
        Integer status
) {
}
