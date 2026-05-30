package com.museum.reservation.controller;

import com.museum.reservation.dto.ActivitySaveRequest;
import com.museum.reservation.dto.AdminActivityResponse;
import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.dto.IdResponse;
import com.museum.reservation.service.AdminActivityService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    public AdminActivityController(AdminActivityService adminActivityService) {
        this.adminActivityService = adminActivityService;
    }

    @GetMapping
    public ApiResponse<List<AdminActivityResponse>> listActivities(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.ok(adminActivityService.listActivities(status, startDate, endDate));
    }

    @PostMapping
    public ApiResponse<IdResponse> createActivity(@Valid @RequestBody ActivitySaveRequest request) {
        return ApiResponse.ok("新增成功", adminActivityService.createActivity(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivitySaveRequest request) {
        adminActivityService.updateActivity(id, request);
        return ApiResponse.ok("修改成功", null);
    }
}
