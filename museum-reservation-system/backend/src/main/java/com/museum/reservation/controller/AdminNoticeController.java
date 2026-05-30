package com.museum.reservation.controller;

import com.museum.reservation.dto.AdminNoticeResponse;
import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.dto.IdResponse;
import com.museum.reservation.dto.NoticeSaveRequest;
import com.museum.reservation.service.AdminNoticeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    public AdminNoticeController(AdminNoticeService adminNoticeService) {
        this.adminNoticeService = adminNoticeService;
    }

    @GetMapping
    public ApiResponse<List<AdminNoticeResponse>> listNotices(
            @RequestParam(required = false) Integer enabled,
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.ok(adminNoticeService.listNotices(enabled, type));
    }

    @PostMapping
    public ApiResponse<IdResponse> createNotice(@Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.ok("新增成功", adminNoticeService.createNotice(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest request) {
        adminNoticeService.updateNotice(id, request);
        return ApiResponse.ok("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotice(@PathVariable Long id) {
        adminNoticeService.deleteNotice(id);
        return ApiResponse.ok("删除成功", null);
    }
}
