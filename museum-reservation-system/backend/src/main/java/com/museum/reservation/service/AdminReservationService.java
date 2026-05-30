package com.museum.reservation.service;

import com.museum.reservation.dto.AdminReservationResponse;
import com.museum.reservation.dto.AdminSummaryResponse;
import com.museum.reservation.exception.BusinessException;
import com.museum.reservation.repository.ReservationRecordRepository;
import com.museum.reservation.repository.SummaryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminReservationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReservationRecordRepository reservationRecordRepository;
    private final SummaryRepository summaryRepository;

    public AdminReservationService(ReservationRecordRepository reservationRecordRepository, SummaryRepository summaryRepository) {
        this.reservationRecordRepository = reservationRecordRepository;
        this.summaryRepository = summaryRepository;
    }

    public List<AdminReservationResponse> listReservations(LocalDate visitDate, Long slotId, String idCard, String status) {
        if (slotId != null && slotId <= 0) {
            throw new BusinessException("预约时段 ID 不正确");
        }
        validateIdCard(idCard);
        validateStatus(status);
        return reservationRecordRepository.findAdminReservations(visitDate, slotId, idCard, status);
    }

    public AdminSummaryResponse getSummary(LocalDate visitDate) {
        return summaryRepository.getSummary(visitDate);
    }

    public String exportReservationsCsv(LocalDate visitDate, Long slotId) {
        if (slotId != null && slotId <= 0) {
            throw new BusinessException("预约时段 ID 不正确");
        }
        List<AdminReservationResponse> reservations = reservationRecordRepository.findAdminReservations(visitDate, slotId, null, null);
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("预约编号,游客姓名,身份证号,手机号,参观日期,预约时段,预约状态,创建时间\n");
        for (AdminReservationResponse reservation : reservations) {
            csv.append(csvValue(reservation.reservationNo())).append(',')
                    .append(csvValue(reservation.visitorName())).append(',')
                    .append(csvValue(reservation.idCard())).append(',')
                    .append(csvValue(reservation.phone())).append(',')
                    .append(csvValue(reservation.visitDate() == null ? null : reservation.visitDate().toString())).append(',')
                    .append(csvValue(reservation.slotName())).append(',')
                    .append(csvValue(reservation.status())).append(',')
                    .append(csvValue(reservation.createdAt() == null ? null : reservation.createdAt().format(DATE_TIME_FORMATTER))).append('\n');
        }
        return csv.toString();
    }

    private void validateIdCard(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            return;
        }
        if (!idCard.matches("^[0-9]{17}[0-9Xx]$")) {
            throw new BusinessException("身份证号格式不正确");
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        if (!status.equals("SUCCESS") && !status.equals("CANCELLED")) {
            throw new BusinessException("预约状态不正确");
        }
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
