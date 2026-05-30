package com.museum.reservation.controller;

import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.dto.MuseumInfoResponse;
import com.museum.reservation.dto.MuseumInfoUpdateRequest;
import com.museum.reservation.service.AdminMuseumService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/museum")
public class AdminMuseumController {

    private final AdminMuseumService adminMuseumService;

    public AdminMuseumController(AdminMuseumService adminMuseumService) {
        this.adminMuseumService = adminMuseumService;
    }

    @GetMapping("/info")
    public ApiResponse<MuseumInfoResponse> getMuseumInfo() {
        return ApiResponse.ok(adminMuseumService.getMuseumInfo());
    }

    @PutMapping("/info")
    public ApiResponse<Void> updateMuseumInfo(@Valid @RequestBody MuseumInfoUpdateRequest request) {
        adminMuseumService.updateMuseumInfo(request);
        return ApiResponse.ok("修改成功", null);
    }
}
