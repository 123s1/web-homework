package com.museum.reservation.service;

import com.museum.reservation.dto.AdminActivityResponse;
import com.museum.reservation.dto.AdminSlotResponse;
import com.museum.reservation.dto.MyReservationResponse;
import com.museum.reservation.dto.ReservationCreateRequest;
import com.museum.reservation.dto.ReservationCreateResponse;
import com.museum.reservation.entity.ReservationRecord;
import com.museum.reservation.entity.Visitor;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.ActivityRepository;
import com.museum.reservation.repository.ReservationRecordRepository;
import com.museum.reservation.repository.SlotRepository;
import com.museum.reservation.repository.VisitorRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private static final DateTimeFormatter RESERVATION_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final VisitorRepository visitorRepository;
    private final SlotRepository slotRepository;
    private final ActivityRepository activityRepository;
    private final ReservationRecordRepository reservationRecordRepository;

    public ReservationService(VisitorRepository visitorRepository, SlotRepository slotRepository, ActivityRepository activityRepository, ReservationRecordRepository reservationRecordRepository) {
        this.visitorRepository = visitorRepository;
        this.slotRepository = slotRepository;
        this.activityRepository = activityRepository;
        this.reservationRecordRepository = reservationRecordRepository;
    }

    @Transactional
    public ReservationCreateResponse createReservation(ReservationCreateRequest request) {
        String normalizedIdCard = request.idCard().toUpperCase();
        Visitor visitor = getOrCreateVisitor(request, normalizedIdCard);
        visitorRepository.lockById(visitor.id());
        AdminSlotResponse slot = slotRepository.findById(request.slotId())
                .orElseThrow(() -> new BusinessException("预约时段不存在"));
        AdminActivityResponse activity = activityRepository.findById(slot.activityId())
                .orElseThrow(() -> new BusinessException("预约活动不存在"));
        validateReservationRule(normalizedIdCard, slot, activity);
        int updatedRows = slotRepository.increaseBookedCount(slot.id());
        if (updatedRows != 1) {
            throw new BusinessException("预约名额已满");
        }
        String reservationNo = generateReservationNo(slot.id());
        String qrContent = reservationNo + "|" + normalizedIdCard + "|" + slot.visitDate() + "|" + slot.slotName();
        Long reservationId;
        try {
            reservationId = reservationRecordRepository.insert(
                    reservationNo,
                    visitor.id(),
                    activity.id(),
                    slot.id(),
                    normalizedIdCard,
                    request.phone(),
                    slot.visitDate(),
                    slot.slotName(),
                    qrContent
            );
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("您已预约过该时段，请勿重复预约");
        }
        if (reservationId == null) {
            throw new BusinessException("预约记录创建失败");
        }
        ReservationRecord record = reservationRecordRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("预约记录创建失败"));
        return new ReservationCreateResponse(
                record.id(),
                record.reservationNo(),
                request.name(),
                record.idCard(),
                record.phone(),
                record.visitDate(),
                record.slotName(),
                slot.startTime(),
                slot.endTime(),
                record.qrContent(),
                record.createdAt()
        );
    }

    public List<MyReservationResponse> listMyReservations(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            throw new BusinessException("身份证号不能为空");
        }
        if (!idCard.matches("^[0-9]{17}[0-9Xx]$")) {
            throw new BusinessException("身份证号格式不正确");
        }
        return reservationRecordRepository.findMyReservationsByIdCard(idCard.toUpperCase());
    }

    private Visitor getOrCreateVisitor(ReservationCreateRequest request, String normalizedIdCard) {
        return visitorRepository.findByIdCard(normalizedIdCard)
                .map(visitor -> {
                    int rows = visitorRepository.updateById(visitor.id(), request.name(), request.phone());
                    if (rows != 1) {
                        throw new BusinessException("游客信息更新失败");
                    }
                    return new Visitor(visitor.id(), request.name(), normalizedIdCard, request.phone());
                })
                .orElseGet(() -> {
                    try {
                        Long visitorId = visitorRepository.insert(request.name(), normalizedIdCard, request.phone());
                        if (visitorId == null) {
                            throw new BusinessException("游客信息创建失败");
                        }
                        return new Visitor(visitorId, request.name(), normalizedIdCard, request.phone());
                    } catch (DuplicateKeyException exception) {
                        return visitorRepository.findByIdCard(normalizedIdCard)
                                .orElseThrow(() -> new BusinessException("游客信息创建失败"));
                    }
                });
    }

    private void validateReservationRule(String normalizedIdCard, AdminSlotResponse slot, AdminActivityResponse activity) {
        if (!"OPEN".equals(activity.status())) {
            throw new BusinessException("当前预约活动未开放");
        }
        if (slot.enabled() == null || slot.enabled() != 1) {
            throw new BusinessException("预约时段不可用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.bookingStart())) {
            throw new BusinessException("预约尚未开始");
        }
        if (now.isAfter(activity.bookingEnd())) {
            throw new BusinessException("预约已经结束");
        }
        if (reservationRecordRepository.existsSuccessByIdCardAndSlot(normalizedIdCard, slot.id())) {
            throw new BusinessException("您已预约过该时段，请勿重复预约");
        }
        int personLimit = activity.personLimit() == null ? 1 : activity.personLimit();
        int alreadyBooked = reservationRecordRepository.countSuccessByIdCardAndVisitDate(normalizedIdCard, slot.visitDate());
        if (alreadyBooked >= personLimit) {
            throw new BusinessException("您当天的预约次数已达上限（每人每天最多 " + personLimit + " 场）");
        }
        if (slot.remaining() == null || slot.remaining() <= 0) {
            throw new BusinessException("预约名额已满");
        }
    }

    private String generateReservationNo(Long slotId) {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "MR" + LocalDateTime.now().format(RESERVATION_NO_TIME_FORMATTER) + random + slotId;
    }
}
