package com.museum.reservation.service;

import com.museum.reservation.dto.MuseumInfoResponse;
import com.museum.reservation.dto.MuseumInfoUpdateRequest;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.MuseumInfoRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminMuseumService {

    private final MuseumInfoRepository museumInfoRepository;

    public AdminMuseumService(MuseumInfoRepository museumInfoRepository) {
        this.museumInfoRepository = museumInfoRepository;
    }

    public MuseumInfoResponse getMuseumInfo() {
        return museumInfoRepository.findFirst()
                .orElseThrow(() -> new BusinessException("未配置场馆信息"));
    }

    public void updateMuseumInfo(MuseumInfoUpdateRequest request) {
        MuseumInfoResponse museumInfo = getMuseumInfo();
        int rows = museumInfoRepository.updateById(museumInfo.id(), request);
        if (rows != 1) {
            throw new BusinessException("场馆信息修改失败");
        }
    }
}
