package com.museum.reservation.controller;

import com.museum.reservation.dto.AdminSystemStatusResponse;
import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.service.SystemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final SystemService systemService;

    public HealthController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(systemService.health());
    }

    @GetMapping("/admin/status")
    public ApiResponse<AdminSystemStatusResponse> adminStatus() {
        return ApiResponse.ok(systemService.adminStatus());
    }
}
