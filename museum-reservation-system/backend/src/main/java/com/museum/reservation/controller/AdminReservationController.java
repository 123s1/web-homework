package com.museum.reservation.controller;

import com.museum.reservation.dto.AdminReservationResponse;
import com.museum.reservation.dto.AdminSummaryResponse;
import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.service.AdminReservationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @GetMapping("/reservations")
    public ApiResponse<List<AdminReservationResponse>> listReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(required = false) Long slotId,
            @RequestParam(required = false) String idCard,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(adminReservationService.listReservations(visitDate, slotId, idCard, status));
    }

    @GetMapping("/reservations/export")
    public ResponseEntity<String> exportReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(required = false) Long slotId
    ) {
        String csv = adminReservationService.exportReservationsCsv(visitDate, slotId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reservations.csv")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv);
    }

    @GetMapping("/summary")
    public ApiResponse<AdminSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate
    ) {
        return ApiResponse.ok(adminReservationService.getSummary(visitDate));
    }
}
