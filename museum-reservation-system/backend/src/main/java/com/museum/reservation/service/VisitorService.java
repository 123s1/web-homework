package com.museum.reservation.service;

import com.museum.reservation.dto.VisitorLoginRequest;
import com.museum.reservation.dto.VisitorLoginResponse;
import com.museum.reservation.entity.Visitor;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.VisitorRepository;
import org.springframework.stereotype.Service;

@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;

    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    public VisitorLoginResponse login(VisitorLoginRequest request) {
        String normalizedIdCard = request.idCard().toUpperCase();
        return visitorRepository.findByIdCard(normalizedIdCard)
                .map(visitor -> updateExistingVisitor(visitor, request, normalizedIdCard))
                .orElseGet(() -> createVisitor(request, normalizedIdCard));
    }

    private VisitorLoginResponse updateExistingVisitor(Visitor visitor, VisitorLoginRequest request, String normalizedIdCard) {
        int rows = visitorRepository.updateById(visitor.id(), request);
        if (rows != 1) {
            throw new BusinessException("游客信息更新失败");
        }
        return new VisitorLoginResponse(visitor.id(), request.name(), normalizedIdCard, request.phone());
    }

    private VisitorLoginResponse createVisitor(VisitorLoginRequest request, String normalizedIdCard) {
        Long id = visitorRepository.insert(request);
        if (id == null) {
            throw new BusinessException("游客登录失败");
        }
        return new VisitorLoginResponse(id, request.name(), normalizedIdCard, request.phone());
    }
}
