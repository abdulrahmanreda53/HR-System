package com.company.hr.service;

import com.company.hr.dto.Dtos.LeaveBalanceResponse;
import com.company.hr.dto.Dtos.LeaveDecisionRequest;
import com.company.hr.dto.Dtos.LeaveResponse;
import com.company.hr.dto.Dtos.LeaveSubmitRequest;
import com.company.hr.entity.LeaveRequest;
import com.company.hr.entity.User;
import com.company.hr.enums.LeaveStatus;
import com.company.hr.exception.BusinessException;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.repository.LeaveRequestRepository;
import com.company.hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private static final List<LeaveStatus> RESERVING = List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED);

    private final LeaveRequestRepository leaveRepository;
    private final UserRepository userRepository;

    @Transactional
    public LeaveResponse submit(Long userId, LeaveSubmitRequest req) {
        if (req.endDate().isBefore(req.startDate())) {
            throw new BusinessException("End date must not be before start date");
        }
        User user = findUser(userId);

        long overlaps = leaveRepository.countOverlapping(userId, RESERVING, req.startDate(), req.endDate());
        if (overlaps > 0) {
            throw new BusinessException("You already have a pending or approved leave overlapping these dates");
        }
        checkBalance(user, req.startDate(), req.endDate());

        LeaveRequest leave = leaveRepository.save(LeaveRequest.builder()
                .user(user).startDate(req.startDate()).endDate(req.endDate())
                .reason(req.reason()).status(LeaveStatus.PENDING).build());
        return LeaveResponse.from(leave);
    }

    @Transactional
    public LeaveResponse decide(Long leaveId, LeaveDecisionRequest req) {
        if (req.status() == LeaveStatus.PENDING) {
            throw new BusinessException("Decision must be APPROVED or REJECTED");
        }
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveId));
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Leave request has already been " + leave.getStatus());
        }
        leave.setStatus(req.status());
        leave.setHrNotes(req.hrNotes());
        return LeaveResponse.from(leave);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> findMine(Long userId) {
        return leaveRepository.findByUserIdOrderByStartDateDesc(userId)
                .stream().map(LeaveResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> findAll(LeaveStatus status) {
        List<LeaveRequest> leaves = status == null
                ? leaveRepository.findAllByOrderByStartDateDesc()
                : leaveRepository.findByStatusOrderByStartDateAsc(status);
        return leaves.stream().map(LeaveResponse::from).toList();
    }


    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(Long userId, YearMonth month) {
        User user = findUser(userId);
        int taken = sumDays(userId, List.of(LeaveStatus.APPROVED), month);
        int pending = sumDays(userId, List.of(LeaveStatus.PENDING), month);
        int allowance = user.getMonthlyLeaveDays();
        int remaining = Math.max(0, allowance - taken - pending);
        return new LeaveBalanceResponse(userId, month.toString(), allowance, taken, pending, remaining);
    }




    private void checkBalance(User user, LocalDate start, LocalDate end) {
        YearMonth last = YearMonth.from(end);
        for (YearMonth m = YearMonth.from(start); !m.isAfter(last); m = m.plusMonths(1)) {
            int requested = overlapDays(start, end, m);
            int reserved = sumDays(user.getId(), RESERVING, m);
            int allowance = user.getMonthlyLeaveDays();
            if (reserved + requested > allowance) {
                throw new BusinessException(
                        "Not enough leave balance for %s: allowance %d, already used or pending %d, requested %d"
                                .formatted(m, allowance, reserved, requested));
            }
        }
    }

    private int sumDays(Long userId, Collection<LeaveStatus> statuses, YearMonth month) {
        return leaveRepository.findOverlapping(userId, statuses, month.atDay(1), month.atEndOfMonth())
                .stream()
                .mapToInt(l -> overlapDays(l.getStartDate(), l.getEndDate(), month))
                .sum();
    }


    private static int overlapDays(LocalDate start, LocalDate end, YearMonth month) {
        LocalDate from = start.isAfter(month.atDay(1)) ? start : month.atDay(1);
        LocalDate to = end.isBefore(month.atEndOfMonth()) ? end : month.atEndOfMonth();
        return to.isBefore(from) ? 0 : (int) ChronoUnit.DAYS.between(from, to) + 1;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
