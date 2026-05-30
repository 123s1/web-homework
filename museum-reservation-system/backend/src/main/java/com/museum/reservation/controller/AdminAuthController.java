package com.museum.reservation.controller;

import com.museum.reservation.dto.AdminLoginRequest;
import com.museum.reservation.dto.AdminLoginResponse;
import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok("登录成功", adminService.login(request));
    }
}
