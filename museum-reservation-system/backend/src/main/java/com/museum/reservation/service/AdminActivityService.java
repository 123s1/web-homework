package com.museum.reservation.service;

import com.museum.reservation.dto.ActivitySaveRequest;
import com.museum.reservation.dto.AdminActivityResponse;
import com.museum.reservation.dto.IdResponse;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminActivityService {

    private final ActivityRepository activityRepository;

    public AdminActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<AdminActivityResponse> listActivities(String status, LocalDate startDate, LocalDate endDate) {
        validateStatus(status);
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        return activityRepository.findAll(status, startDate, endDate);
    }

    public IdResponse createActivity(ActivitySaveRequest request) {
        validateRequest(request, null);
        Long id = activityRepository.insert(request);
        if (id == null) {
            throw new BusinessException("预约活动新增失败");
        }
        return new IdResponse(id);
    }

    public void updateActivity(Long id, ActivitySaveRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException("预约活动 ID 不正确");
        }
        AdminActivityResponse oldActivity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约活动不存在"));
        validateRequest(request, id);
        Integer bookedCount = activityRepository.sumSlotBookedCount(id);
        if (request.dailyCapacity() < bookedCount) {
            throw new BusinessException("每日总名额不能小于已预约人数");
        }
        if (!oldActivity.visitDate().equals(request.visitDate()) && bookedCount > 0) {
            throw new BusinessException("已有预约记录的活动不能修改参观日期");
        }
        int rows = activityRepository.updateById(id, request);
        if (rows != 1) {
            throw new BusinessException("预约活动修改失败");
        }
    }

    private void validateRequest(ActivitySaveRequest request, Long excludeId) {
        validateStatus(request.status());
        if (!request.bookingEnd().isAfter(request.bookingStart())) {
            throw new BusinessException("预约结束时间必须晚于预约开始时间");
        }
        if (activityRepository.existsByVisitDate(request.visitDate(), excludeId)) {
            throw new BusinessException("同一参观日期已存在预约活动");
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        if (!status.equals("DRAFT") && !status.equals("OPEN") && !status.equals("CLOSED") && !status.equals("DISABLED")) {
            throw new BusinessException("活动状态不正确");
        }
    }
}
