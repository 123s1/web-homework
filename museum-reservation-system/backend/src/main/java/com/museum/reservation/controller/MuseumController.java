package com.museum.reservation.controller;

import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.dto.AvailableSlotResponse;
import com.museum.reservation.dto.MuseumInfoResponse;
import com.museum.reservation.dto.NoticeResponse;
import com.museum.reservation.service.MuseumService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MuseumController {

    private final MuseumService museumService;

    public MuseumController(MuseumService museumService) {
        this.museumService = museumService;
    }

    @GetMapping("/museum/info")
    public ApiResponse<MuseumInfoResponse> getMuseumInfo() {
        return ApiResponse.ok(museumService.getMuseumInfo());
    }

    @GetMapping("/notices")
    public ApiResponse<List<NoticeResponse>> listEnabledNotices() {
        return ApiResponse.ok(museumService.listEnabledNotices());
    }

    @GetMapping("/slots")
    public ApiResponse<List<AvailableSlotResponse>> listAvailableSlots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate
    ) {
        return ApiResponse.ok(museumService.listAvailableSlots(visitDate));
    }
}
