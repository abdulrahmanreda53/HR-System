package com.company.hr.controller;

import com.company.hr.dto.Dtos.AttendanceResponse;
import com.company.hr.security.AppUserDetails;
import com.company.hr.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/me")
    public List<AttendanceResponse> mine(
            @AuthenticationPrincipal AppUserDetails me,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.getAttendance(me.getId(), startOrDefault(from), endOrDefault(to));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('HR')")
    public List<AttendanceResponse> forUser(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.getAttendance(userId, startOrDefault(from), endOrDefault(to));
    }

    private LocalDate startOrDefault(LocalDate from) {
        return from != null ? from : LocalDate.now().withDayOfMonth(1);
    }

    private LocalDate endOrDefault(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }
}
