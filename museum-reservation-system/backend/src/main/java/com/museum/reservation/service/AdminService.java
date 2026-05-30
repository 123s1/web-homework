package com.museum.reservation.service;

import com.museum.reservation.dto.AdminLoginRequest;
import com.museum.reservation.dto.AdminLoginResponse;
import com.museum.reservation.entity.AdminUser;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser adminUser = adminRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("管理员账号或密码错误"));
        if (adminUser.status() == null || adminUser.status() != 1) {
            throw new BusinessException("管理员账号已被禁用");
        }
        if (!adminUser.password().equals(request.password())) {
            throw new BusinessException("管理员账号或密码错误");
        }
        return new AdminLoginResponse(adminUser.id(), adminUser.username(), adminUser.role());
    }
}
