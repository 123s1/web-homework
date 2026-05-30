package com.museum.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VisitorLoginRequest(
        @NotBlank(message = "姓名不能为空")
        String name,
        @NotBlank(message = "身份证号不能为空")
        @Pattern(regexp = "^[0-9]{17}[0-9Xx]$", message = "身份证号格式不正确")
        String idCard,
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9][0-9]{9}$", message = "手机号格式不正确")
        String phone
) {
}
