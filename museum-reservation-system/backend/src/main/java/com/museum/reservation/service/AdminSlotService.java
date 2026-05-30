package com.museum.reservation.service;

import com.museum.reservation.dto.AdminActivityResponse;
import com.museum.reservation.dto.AdminSlotResponse;
import com.museum.reservation.dto.IdResponse;
import com.museum.reservation.dto.SlotCreateRequest;
import com.museum.reservation.dto.SlotUpdateRequest;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.ActivityRepository;
import com.museum.reservation.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminSlotService {

    private final SlotRepository slotRepository;
    private final ActivityRepository activityRepository;

    public AdminSlotService(SlotRepository slotRepository, ActivityRepository activityRepository) {
        this.slotRepository = slotRepository;
        this.activityRepository = activityRepository;
    }

    public List<AdminSlotResponse> listSlots(Long activityId, LocalDate visitDate) {
        if (activityId != null && activityId <= 0) {
            throw new BusinessException("预约活动 ID 不正确");
        }
        return slotRepository.findAll(activityId, visitDate);
    }

    public IdResponse createSlot(SlotCreateRequest request) {
        AdminActivityResponse activity = activityRepository.findById(request.activityId())
                .orElseThrow(() -> new BusinessException("预约活动不存在"));
        validateTime(request.startTime(), request.endTime());
        validateDuplicateTime(request.activityId(), request.startTime(), request.endTime(), null);
        validateActivityCapacity(request.activityId(), request.totalCapacity(), request.enabled(), null, activity.dailyCapacity());
        Long id = slotRepository.insert(request, activity.visitDate());
        if (id == null) {
            throw new BusinessException("预约时段新增失败");
        }
        return new IdResponse(id);
    }

    public void updateSlot(Long id, SlotUpdateRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException("预约时段 ID 不正确");
        }
        AdminSlotResponse oldSlot = slotRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约时段不存在"));
        AdminActivityResponse activity = activityRepository.findById(oldSlot.activityId())
                .orElseThrow(() -> new BusinessException("预约活动不存在"));
        validateTime(request.startTime(), request.endTime());
        validateDuplicateTime(oldSlot.activityId(), request.startTime(), request.endTime(), id);
        if (request.totalCapacity() < oldSlot.bookedCount()) {
            throw new BusinessException("时段总名额不能小于已预约人数");
        }
        if (oldSlot.bookedCount() > 0 && (!oldSlot.startTime().equals(request.startTime()) || !oldSlot.endTime().equals(request.endTime()))) {
            throw new BusinessException("已有预约记录的时段不能修改入场时间");
        }
        validateActivityCapacity(oldSlot.activityId(), request.totalCapacity(), request.enabled(), id, activity.dailyCapacity());
        int rows = slotRepository.updateById(id, request);
        if (rows != 1) {
            throw new BusinessException("预约时段修改失败");
        }
    }

    private void validateTime(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("入场结束时间必须晚于入场开始时间");
        }
    }

    private void validateDuplicateTime(Long activityId, java.time.LocalTime startTime, java.time.LocalTime endTime, Long excludeId) {
        if (slotRepository.existsByActivityAndTime(activityId, startTime, endTime, excludeId)) {
            throw new BusinessException("同一活动下已存在相同时间段");
        }
    }

    private void validateActivityCapacity(Long activityId, Integer currentCapacity, Integer enabled, Long excludeId, Integer dailyCapacity) {
        if (enabled == null || enabled == 0) {
            return;
        }
        Integer otherEnabledCapacity = slotRepository.sumEnabledCapacity(activityId, excludeId);
        if (otherEnabledCapacity + currentCapacity > dailyCapacity) {
            throw new BusinessException("启用时段名额总和不能超过活动每日总名额");
        }
    }
}
