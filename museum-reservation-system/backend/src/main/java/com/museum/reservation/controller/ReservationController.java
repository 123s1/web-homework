package com.museum.reservation.controller;

import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.dto.MyReservationResponse;
import com.museum.reservation.dto.ReservationCreateRequest;
import com.museum.reservation.dto.ReservationCreateResponse;
import com.museum.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ApiResponse<ReservationCreateResponse> createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        return ApiResponse.ok("预约成功", reservationService.createReservation(request));
    }

    @GetMapping("/my")
    public ApiResponse<List<MyReservationResponse>> listMyReservations(@RequestParam(required = false) String idCard) {
        return ApiResponse.ok(reservationService.listMyReservations(idCard));
    }
}
