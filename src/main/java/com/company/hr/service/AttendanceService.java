package com.company.hr.service;

import com.company.hr.dto.Dtos.AttendanceResponse;
import com.company.hr.entity.Attendance;
import com.company.hr.entity.User;
import com.company.hr.enums.AttendanceType;
import com.company.hr.enums.TransactionSource;
import com.company.hr.enums.TransactionType;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.repository.AttendanceRepository;
import com.company.hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    public enum PunchOutcome { RECORDED, DUPLICATE, UNKNOWN_USER, INACTIVE_USER }

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final SystemSettingService settingService;
    private final PayrollService payrollService;


    @Transactional
    public PunchOutcome recordPunch(String machineId, LocalDateTime timestamp, AttendanceType type) {
        User user = userRepository.findByMachineId(machineId).orElse(null);
        if (user == null) {
            log.warn("Punch ignored: no user linked to machineId={}", machineId);
            return PunchOutcome.UNKNOWN_USER;
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Punch ignored: user {} is inactive", user.getId());
            return PunchOutcome.INACTIVE_USER;
        }
        if (attendanceRepository.existsByUserIdAndTimestampAndType(user.getId(), timestamp, type)) {
            return PunchOutcome.DUPLICATE;
        }

        Attendance attendance = Attendance.builder()
                .user(user).timestamp(timestamp).type(type).build();

        if (type == AttendanceType.CHECK_IN && isFirstCheckInOfDay(user.getId(), timestamp)) {
            long lateMinutes = calculateLateMinutes(timestamp);
            if (lateMinutes > 0) {
                attendance.setIsLate(true);
                attendance.setLateMinutes((int) lateMinutes);
            }
        }

        attendanceRepository.save(attendance);

        if (Boolean.TRUE.equals(attendance.getIsLate())) {
            applyLateDeduction(user, attendance);
        }
        return PunchOutcome.RECORDED;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendance(Long userId, LocalDate from, LocalDate to) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return attendanceRepository
                .findByUserIdAndTimestampBetweenOrderByTimestampAsc(userId, from.atStartOfDay(), to.atTime(LocalTime.MAX))
                .stream().map(AttendanceResponse::from).toList();
    }




    private boolean isFirstCheckInOfDay(Long userId, LocalDateTime timestamp) {
        return attendanceRepository.countByUserIdAndTypeAndTimestampBetween(
                userId, AttendanceType.CHECK_IN, timestamp.toLocalDate().atStartOfDay(), timestamp) == 0;
    }


    private long calculateLateMinutes(LocalDateTime timestamp) {
        LocalTime workStart = settingService.getLocalTime(SystemSettingService.WORK_START_TIME);
        LocalDateTime scheduled = timestamp.toLocalDate().atTime(workStart);
        return timestamp.isAfter(scheduled) ? Duration.between(scheduled, timestamp).toMinutes() : 0;
    }


    private void applyLateDeduction(User user, Attendance attendance) {
        if (user.getBaseSalary().signum() <= 0) {
            log.info("User {} has no base salary; late deduction skipped", user.getId());
            return;
        }
        int threshold = settingService.getInt(SystemSettingService.LATE_LONG_THRESHOLD_MINUTES);
        String key = attendance.getLateMinutes() > threshold
                ? SystemSettingService.LATE_LONG_DEDUCTION_DAYS
                : SystemSettingService.LATE_DEDUCTION_DAYS;
        BigDecimal days = settingService.getBigDecimal(key);
        if (days.signum() <= 0) {
            log.info("{} is zero; no deduction created for user {}", key, user.getId());
            return;
        }
        LocalDate date = attendance.getTimestamp().toLocalDate();
        String reason = "Late check-in on %s by %d minute(s)".formatted(date, attendance.getLateMinutes());
        payrollService.recordByDays(user, days, TransactionType.DEDUCTION, TransactionSource.AUTOMATIC, reason, date);
        log.info("Automatic late deduction of {} day(s) created for user {} ({})", days, user.getId(), reason);
    }
}
