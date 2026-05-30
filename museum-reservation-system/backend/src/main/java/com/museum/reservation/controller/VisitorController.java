package com.museum.reservation.controller;

import com.museum.reservation.dto.ApiResponse;
import com.museum.reservation.dto.VisitorLoginRequest;
import com.museum.reservation.dto.VisitorLoginResponse;
import com.museum.reservation.service.VisitorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/login")
    public ApiResponse<VisitorLoginResponse> login(@Valid @RequestBody VisitorLoginRequest request) {
        return ApiResponse.ok("登录成功", visitorService.login(request));
    }
}
