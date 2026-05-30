package com.museum.reservation.service;

import com.museum.reservation.dto.AdminNoticeResponse;
import com.museum.reservation.dto.IdResponse;
import com.museum.reservation.dto.NoticeSaveRequest;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.NoticeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    public AdminNoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<AdminNoticeResponse> listNotices(Integer enabled, String type) {
        validateEnabled(enabled);
        validateType(type);
        return noticeRepository.findAll(enabled, type);
    }

    public IdResponse createNotice(NoticeSaveRequest request) {
        Long id = noticeRepository.insert(request);
        if (id == null) {
            throw new BusinessException("公告新增失败");
        }
        return new IdResponse(id);
    }

    public void updateNotice(Long id, NoticeSaveRequest request) {
        ensureNoticeExists(id);
        int rows = noticeRepository.updateById(id, request);
        if (rows != 1) {
            throw new BusinessException("公告修改失败");
        }
    }

    public void deleteNotice(Long id) {
        ensureNoticeExists(id);
        int rows = noticeRepository.disableById(id);
        if (rows != 1) {
            throw new BusinessException("公告删除失败");
        }
    }

    private void ensureNoticeExists(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("公告 ID 不正确");
        }
        noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
    }

    private void validateEnabled(Integer enabled) {
        if (enabled != null && enabled != 0 && enabled != 1) {
            throw new BusinessException("启用状态不正确");
        }
    }

    private void validateType(String type) {
        if (type == null || type.isBlank()) {
            return;
        }
        if (!type.equals("NORMAL") && !type.equals("EXHIBITION") && !type.equals("CLOSE") && !type.equals("RULE")) {
            throw new BusinessException("公告类型不正确");
        }
    }
}
