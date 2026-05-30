package com.museum.reservation.controller;

import com.museum.reservation.dto.AdminSlotResponse;
import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.dto.IdResponse;
import com.museum.reservation.dto.SlotCreateRequest;
import com.museum.reservation.dto.SlotUpdateRequest;
import com.museum.reservation.service.AdminSlotService;
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
@RequestMapping("/api/admin/slots")
public class AdminSlotController {

    private final AdminSlotService adminSlotService;

    public AdminSlotController(AdminSlotService adminSlotService) {
        this.adminSlotService = adminSlotService;
    }

    @GetMapping
    public ApiResponse<List<AdminSlotResponse>> listSlots(
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate
    ) {
        return ApiResponse.ok(adminSlotService.listSlots(activityId, visitDate));
    }

    @PostMapping
    public ApiResponse<IdResponse> createSlot(@Valid @RequestBody SlotCreateRequest request) {
        return ApiResponse.ok("新增成功", adminSlotService.createSlot(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateSlot(@PathVariable Long id, @Valid @RequestBody SlotUpdateRequest request) {
        adminSlotService.updateSlot(id, request);
        return ApiResponse.ok("修改成功", null);
    }
}
